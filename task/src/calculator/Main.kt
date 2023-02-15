package calculator

import java.lang.NumberFormatException
import java.util.Scanner
import kotlin.Exception
import kotlin.math.pow
import java.math.BigInteger

class OperationException() : Exception("Invalid expression") {}
class IdentifierException() : Exception("Invalid identifier") {}
class AssignmentException() : Exception("Invalid assignment") {}
class UnknownException() : Exception("Unknown variable") {}


fun sum(a: BigInteger, b: BigInteger): BigInteger = a + b
fun subtrac(a: BigInteger, b: BigInteger): BigInteger = a - b
fun multiply(a: BigInteger, b: BigInteger): BigInteger = a * b
fun divide(a: BigInteger, b: BigInteger): BigInteger = a / b
fun power(a:BigInteger,b:BigInteger):BigInteger = a.pow(b.toInt())
fun helpCommand() = println("The program calculates the sum of numbers using adding and subtracting")
fun fixDoubleOperationSign(str: String): String {
    var res = str
    if (res.contains("**") || res.contains("//")) throw OperationException()
    while (true) { //fix all double signs
        val tmp = res
        res = res.replace("++", "+")
        res = res.replace("--", "+")//fix double signs
        res = res.replace("  ", " ") //remove double spaces
        if (tmp == res) break
    }
    while (true) { //fix sign after all doubles are done
        val tmp = res
        res = res.replace("-+", "-")
        res = res.replace("+-", "-")
        if (tmp == res) break
    }

    return res
}

fun countOperation(a: BigInteger, operation: String, b: BigInteger): BigInteger {
    return when (operation) {
        "+" -> sum(a, b)
        "-" -> subtrac(a, b)
        "*" -> multiply(a, b)
        "/" -> divide(a, b)
        "^" -> power(a, b)
        else -> throw OperationException()
    }
}

//fun processLine(input: String): Int {
//    var result = 0
//    val splitInputs = input.split(" ")
//
//    for (i in splitInputs.indices step 2) {
//        if (i == 0) {
//            result = countOperation(result, "+", variableOrInt(splitInputs[i]))
//        } else {
//            result = countOperation(result, splitInputs[i - 1], variableOrInt(splitInputs[i]))
//        }
//    }
//    return result
//}

fun checkVariableName(input: String) {
    val reg = "[a-zA-Z]*".toRegex()
    if (!reg.matches(input)) throw IdentifierException()
}

fun removeSpaces(input: String): String {
    return input.replace(" ", "")
}

fun saveToMap(input: String) {
    val str = removeSpaces(input)
    val data = str.split("=")
    if (data.size == 2) {
        checkVariableName(data[0])
        variables += Pair(data[0], variableOrInt(data[1]))
    } else {
        throw AssignmentException()
    }

}

fun getVariableValue(str: String): BigInteger {
    checkVariableName(str)
    if (!variables.contains(str)) {
        throw UnknownException()
    }
    return variables.getOrDefault(str, 0.toBigInteger())
}

fun variableOrInt(input: String): BigInteger {
    return if ("[a-zA-Z]".toRegex().matches(input.first().toString())) getVariableValue(input) else input.toBigInteger()
}

val variables = mutableMapOf<String, BigInteger>()

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    while (true) {
        val input = scanner.nextLine()
        if (input.isEmpty()) continue
        if (input == "/exit") break
        if (input == "/help") {
            helpCommand()
            continue
        }
        if (input.first() == '/') {
            println("Unknown command")
            continue
        }

        try {
            if (input.contains("=")) {
                saveToMap(input)
                continue
            }
            val validInput = operatorsValidate(input)
            if (!validInput.contains(" ")) {
                println(variableOrInt(validInput))
                continue
            }
            //println(processLine(validInput))
            //println(postfixConverter(validInput))
            println(posfixCounter(postfixConverter(validInput)))
        } catch (ex: NumberFormatException) {
            println("Invalid expression")
        } catch (ex: Exception) {
            println(ex.message)
        }
    }
    println("Bye!")
}

fun operatorsValidate(input: String): String {
    var res = fixDoubleOperationSign(input)
    res = res.replace("*", " * ")
    res = res.replace("/", " / ")
    res = res.replace("+", " + ")
    res = res.replace("-", " - ")
    res = res.replace("(", " ( ")
    res = res.replace(")", " ) ")
    res = res.replace("^", " ^ ")
    res = res.replace("  ", " ")
    return res
}

fun operatorPriority(operator: String): Int {
    return when (operator) {
        "+", "-" -> 1
        "*", "/" -> 2
        "^" -> 3
        "(" -> 4
        else -> throw OperationException()
    }
}

fun isNotValue(str: String): Boolean {
    return when (str) {
        "+", "-", "*", "/", "(", ")", "^" -> true
        else -> false
    }
}

fun postfixConverter(input: String): String {
    val result = mutableListOf<String>()
    val stack = mutableListOf<String>()
    val inputList = input.split(" ")
    for (inp in inputList) {
        if(inp == ""){
            continue
        }
        if (!isNotValue(inp)) {
            result.add(variableOrInt(inp).toString())
            continue
        }
        if (inp == ")") {
            while (stack.size >= 1 && stack.last() != "(") {
                result.add(stack.removeLast())
            }
            if (stack.size >= 1) {
                stack.removeLast()
            } else {
                throw OperationException()
            }
        } else if (stack.size >= 1 && stack.last() != "(") {
            val inpPriority = operatorPriority(inp)
            while (stack.size >= 1 && inpPriority <= operatorPriority(stack.last())&& stack.last()!="(") {
                result.add(stack.removeLast())
            }
            stack.add(inp)
        }else{
            stack.add(inp)
        }


    }
    while (stack.size >= 1) {
        val last = stack.removeLast()
        if (last == "(") {
            throw OperationException()
        }
        result.add(last)
    }
    return result.joinToString(" ")
}

fun posfixCounter(input: String):BigInteger{
    val stack = mutableListOf<BigInteger>()
    val inputList = input.split(" ")
    for(inp in inputList){
        if(!isNotValue(inp)){
            stack.add(inp.toBigInteger())
            continue
        }
        val b = stack.removeLast()
        val a = stack.removeLast()
        stack.add(countOperation(a,inp,b))
    }
    return stack.last()
}
