package com.example.fitbalance.repository

import com.example.fitbalance.data.Meal
import com.example.fitbalance.data.MealGenerationRequest
import com.example.fitbalance.data.MealItem
import com.google.ai.client.generativeai.GenerativeModel
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor(
    private val generativeModel: GenerativeModel
) {
    suspend fun generateMealPlan(request: MealGenerationRequest): GeminiMealResult {
        return try {
            val prompt = buildMealPrompt(request)
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: return GeminiMealResult.Error("Yanıt alınamadı")

            parseMealResponse(responseText)
        } catch (e: Exception) {
            GeminiMealResult.Error(e.message ?: "Bilinmeyen hata")
        }
    }

    private fun buildMealPrompt(request: MealGenerationRequest): String {
        val goalText = when (request.goal) {
            "Kilo Al" -> "kilo almak"
            "Kilo Ver" -> "kilo vermek"
            "Kilonu Koru" -> "kilosunu korumak"
            else -> "sağlıklı beslenmek"
        }

        val previousMealsText = if (request.previousMeals.isNotEmpty()) {
            "Kullanıcının geçmişte tükettiği yiyecekler: ${request.previousMeals.take(20).joinToString(", ")}. Bu yiyecekleri göz önünde bulundurarak benzer veya sevebileceği yiyecekler öner."
        } else {
            ""
        }

        return """
            Bir fitness uygulaması için günlük öğün planı oluştur. Kullanıcı bilgileri:
            - Boy: ${request.height} cm
            - Kilo: ${request.weight} kg
            - Yaş: ${request.age}
            - Cinsiyet: ${request.gender}
            - Hedef: $goalText
            $previousMealsText
            
            Lütfen SADECE aşağıdaki JSON formatında yanıt ver, başka hiçbir açıklama ekleme:
            {
              "breakfast": {
                "items": [
                  {"name": "Yiyecek adı", "calories": kalori_sayısı}
                ]
              },
              "lunch": {
                "items": [
                  {"name": "Yiyecek adı", "calories": kalori_sayısı}
                ]
              },
              "dinner": {
                "items": [
                  {"name": "Yiyecek adı", "calories": kalori_sayısı}
                ]
              }
            }
            
            Kurallar:
            - Her öğün için 3-5 yiyecek öner
            - Kalori değerleri gerçekçi olsun
            - Türk mutfağına uygun yiyecekler öner
            - Sadece JSON formatında yanıt ver
        """.trimIndent()
    }

    private fun parseMealResponse(responseText: String): GeminiMealResult {
        return try {
            val cleanedText = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanedText)
            val breakfast = parseMeal(jsonObject.getJSONObject("breakfast"))
            val lunch = parseMeal(jsonObject.getJSONObject("lunch"))
            val dinner = parseMeal(jsonObject.getJSONObject("dinner"))

            GeminiMealResult.Success(breakfast, lunch, dinner)
        } catch (e: Exception) {
            GeminiMealResult.Error("Yanıt işlenemedi: ${e.message}")
        }
    }

    suspend fun generateSingleMeal(
        request: MealGenerationRequest,
        mealType: String,
        previousMealNames: List<String>
    ): GeminiMealResult {
        return try {
            val prompt = buildSingleMealPrompt(request, mealType, previousMealNames)
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: return GeminiMealResult.Error("Yanıt alınamadı")

            parseSingleMealResponse(responseText, mealType)
        } catch (e: Exception) {
            GeminiMealResult.Error(e.message ?: "Bilinmeyen hata")
        }
    }

    private fun buildSingleMealPrompt(
        request: MealGenerationRequest,
        mealType: String,
        previousMealNames: List<String>
    ): String {
        val goalText = when (request.goal) {
            "Kilo Al" -> "kilo almak"
            "Kilo Ver" -> "kilo vermek"
            "Kilonu Koru" -> "kilosunu korumak"
            else -> "sağlıklı beslenmek"
        }

        val mealTypeTurkish = when (mealType) {
            "breakfast" -> "kahvaltı"
            "lunch" -> "öğle yemeği"
            "dinner" -> "akşam yemeği"
            else -> "öğün"
        }

        val previousMealsText = if (previousMealNames.isNotEmpty()) {
            "Kullanıcının bu öğün türünde geçmişte tükettiği yiyecekler: ${previousMealNames.joinToString(", ")}. Bu yiyecekleri göz önünde bulundurarak FARKLI ama benzer lezzette yiyecekler öner."
        } else {
            ""
        }

        return """
        Bir fitness uygulaması için $mealTypeTurkish öğünü oluştur. Kullanıcı bilgileri:
        - Boy: ${request.height} cm
        - Kilo: ${request.weight} kg
        - Yaş: ${request.age}
        - Cinsiyet: ${request.gender}
        - Hedef: $goalText
        $previousMealsText
        
        Lütfen SADECE aşağıdaki JSON formatında yanıt ver, başka hiçbir açıklama ekleme:
        {
          "items": [
            {"name": "Yiyecek adı", "calories": kalori_sayısı}
          ]
        }
        
        Kurallar:
        - 3-5 yiyecek öner
        - Kalori değerleri gerçekçi olsun
        - Türk mutfağına uygun yiyecekler öner
        - Önceki yiyeceklerden FARKLI seçenekler sun
        - Sadece JSON formatında yanıt ver
    """.trimIndent()
    }

    private fun parseSingleMealResponse(responseText: String, mealType: String): GeminiMealResult {
        return try {
            val cleanedText = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanedText)
            val meal = parseMeal(jsonObject)

            when (mealType) {
                "breakfast" -> GeminiMealResult.Success(meal, Meal(), Meal())
                "lunch" -> GeminiMealResult.Success(Meal(), meal, Meal())
                "dinner" -> GeminiMealResult.Success(Meal(), Meal(), meal)
                else -> GeminiMealResult.Error("Geçersiz öğün tipi")
            }
        } catch (e: Exception) {
            GeminiMealResult.Error("Yanıt işlenemedi: ${e.message}")
        }
    }

    private fun parseMeal(mealJson: JSONObject): Meal {
        val itemsArray = mealJson.getJSONArray("items")
        val items = mutableListOf<MealItem>()

        for (i in 0 until itemsArray.length()) {
            val itemJson = itemsArray.getJSONObject(i)
            items.add(
                MealItem(
                    name = itemJson.getString("name"),
                    calories = itemJson.getInt("calories")
                )
            )
        }

        return Meal(
            name = "",
            items = items,
            totalCalories = items.sumOf { it.calories }
        )
    }
}

sealed class GeminiMealResult {
    data class Success(
        val breakfast: Meal,
        val lunch: Meal,
        val dinner: Meal
    ) : GeminiMealResult()
    data class Error(val message: String) : GeminiMealResult()
}