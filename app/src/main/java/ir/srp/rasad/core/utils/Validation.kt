package ir.srp.rasad.core.utils

import java.lang.StringBuilder
import java.util.regex.Pattern


object Validation {

    private val lowercaseRegex = Regex("[a-z]")
    private val uppercaseRegex = Regex("[A-Z]")
    private val numberRegex = Regex("\\d")
    private const val emailPattern =
        "^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*\$"


    fun checkMobilNumberValidation(mobileNumber: String): String {
        val result = StringBuilder("")

        if (mobileNumber.length != 11)
            result.append("Mobile number must be 11 digits")

        if (!mobileNumber.startsWith("09"))
            result.append("Mobile must start with '09'")

        return result.toString()
    }

    fun checkOtpValidation(otp: String): String {
        val result = StringBuilder("")

        if (otp.length != 6)
            result.append("OTP must be 6 digits")

        return result.toString()
    }

    fun checkUsernameValidation(username: String): String {
        if (username.length >= 60)
            return "Username must be less than 60 characters"

        if (username.length < 8)
            return "Username must be more than 8 characters"

        if (!lowercaseRegex.containsMatchIn(username))
            return "Username must have at least one lowercase letter"

        if (!uppercaseRegex.containsMatchIn(username))
            return "Username must have at least one uppercase letter"

        if (!numberRegex.containsMatchIn(username))
            return "Username must contain number"

        return ""
    }

    fun isEmailValid(email: String): Boolean {
        if (email.length >= 60)
            return false

        val pattern: Pattern = Pattern.compile(emailPattern)
        val matcher = pattern.matcher(email)

        return matcher.matches()
    }
}