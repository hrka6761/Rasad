package ir.srp.rasad.core

import java.util.regex.Pattern


object Validation {

    fun isMobilNumberValid(mobileNumber: String): Boolean {
        if (mobileNumber.length != 11)
            return false

        val mobileNumberPattern = "^09\\d*$"
        val pattern: Pattern = Pattern.compile(mobileNumberPattern)
        val matcher = pattern.matcher(mobileNumber)

        return matcher.matches()
    }

    fun isOtpValid(otp: String): Boolean = otp.length == 6

    fun isUsernameValid(username: String) = username.length < 60

    fun isEmailValid(email: String): Boolean {
        if (email.length >= 60)
            return false

        val mobileNumberPattern =
            "^[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*@[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*\$"
        val pattern: Pattern = Pattern.compile(mobileNumberPattern)
        val matcher = pattern.matcher(email)

        return matcher.matches()
    }
}