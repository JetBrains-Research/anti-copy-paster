import com.intellij.testFramework.TestDataFile;

@TestDataFile
public class Calculator{
    private int currentNumber;

    public Calculator(int number){
        this.currentNumber = number;
    }

    public int getCurrentNumber(){
        return this.currentNumber;
    }
    public int add(int toAdd){
        this.currentNumber += toAdd;
        return this.currentNumber;
    }

    public int subtract(int toSubtract){
        this.currentNumber -= toSubtract;
        return this.currentNumber;
    }

    public int multiply(int toMultiply){
        this.currentNumber = this.currentNumber * toMultiply;
        return this.currentNumber;
    }

    public int divide(int toDivide){
        this.currentNumber = this.currentNumber / toDivide;
        return this.currentNumber;
    }
}