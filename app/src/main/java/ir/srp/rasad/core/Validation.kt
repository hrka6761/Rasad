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
}