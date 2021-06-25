package it.trentabitplus.digitaltextsuite.utils
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import java.util.*

/**
 * This class represents a custom Language class
 * @param code : the 2 character code that identifies the language
 *
 * @author Andrea Pepe
 */
class Language(val code: String) : Comparable<Language> {

    companion object{
        /**
         * This method performs a mapping between the language and
         * a flag of a country that better represents the language
         *
         * @param code : the 2 characters code of the language
         *
         * @author Adrian Petru Baba
         */
        fun getFlag(code : String) : String {
            val flag: String
            when(code){
                "sq" -> flag="🇦🇱"
                "ar" -> flag="🇸🇦"
                "ca" -> flag="🇪🇸"
                "zh" -> flag=getCharacter('c')+ getCharacter('n')
                "cs" -> flag="🇨🇿"
                "da" -> flag="🇩🇰"
                "en" -> flag= "🇬🇧"
                "ka" -> flag="🇬🇪"
                "el" -> flag="🇬🇷"
                "he" -> flag="🇮🇱"
                "hi" -> flag="🇮🇳"
                "ja" -> flag="🇯🇵"
                "ko" -> flag="🇰🇷"
                "fa" -> flag="🇮🇷"
                "sw" -> flag="🇹🇿"
                "te" -> flag="🇮🇳"
                "uk" -> flag="🇺🇦"
                "ur" -> flag="🇵🇰"
                "vi" -> flag="🇻🇳"
                else -> flag= getCharacter(code[0])+ getCharacter(code[1])
            }

            return flag

        }

        /**
         * Identify the language of a String and put the result into the value of result param
         *
         * @param text The string to be analyzed
         * @param result The variable where the method must store the result. Put an observer on it
         */
        fun identifyLanguage(text: String,result: MutableLiveData<String>){
            val languageIdentifier = LanguageIdentification.getClient(
                LanguageIdentificationOptions
                .Builder()
                .setConfidenceThreshold(0.80f)
                .build())
            languageIdentifier.identifyLanguage(text).addOnSuccessListener { langCode ->
                result.value = langCode
            }
        }

        private fun getCharacter(character : Char) : String{
            var res =""
            when(character){
                'a' -> res="🇦"
                'b' -> res="🇧"
                'c' -> res="🇨"
                'd' -> res="🇩"
                'e' -> res="🇪"
                'f' -> res="🇫"
                'g' -> res="🇬"
                'h' -> res="🇭"
                'i' -> res="🇮"
                'j' -> res="🇯"
                'k' -> res="🇰"
                'l' -> res="🇱"
                'm' -> res="🇲"
                'n' -> res="🇳"
                'o' -> res="🇴"
                'p' -> res="🇵"
                'q' -> res="🇶"
                'r' -> res="🇷"
                's' -> res="🇸"
                't' -> res="🇹"
                'u' -> res="🇺"
                'v' -> res="🇻"
                'w' -> res="🇼"
                'x' -> res="🇽"
                'y' -> res="🇾"
                'z' -> res="🇿"
            }
            return res
        }
    }



    val displayName : String
        get() = Locale(code).displayName

    override fun equals(other: Any?): Boolean {
        if (other === this){
            return true
        }

        if(other !is Language){
            return false
        }

        val otherLang = other as Language?
        return otherLang!!.code == code
    }

    override fun toString(): String {
        return displayName
    }

    override fun compareTo(other: Language): Int {
        return this.displayName.compareTo(other.displayName)
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

    fun getFlag() : String{
        return getFlag(this.code)
    }
}