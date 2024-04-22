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
        if (mobileNumber.length != 11)
            return "Mobile number must be 11 digits"

        if (!mobileNumber.startsWith("09"))
            return "Mobile must start with '09'"

        return ""
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

    fun checkPasswordValidation(password: String): String {
        if (password.length >= 30)
            return "Password must be less than 30 characters"

        if (password.length < 16)
            return "Password must be more than 16 characters"

        if (!lowercaseRegex.containsMatchIn(password))
            return "Password must have at least one lowercase letter"

        if (!uppercaseRegex.containsMatchIn(password))
            return "Password must have at least one uppercase letter"

        if (!numberRegex.containsMatchIn(password))
            return "Password must contain number"

        return ""
    }
}