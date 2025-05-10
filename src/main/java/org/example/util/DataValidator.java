package org.example.util;

import net.bytebuddy.implementation.bytecode.Throw;

public class DataValidator {

    public static void ValidateArgsNum(int argsNum){
        if(argsNum != 2){
            throw  new RuntimeException("Invalid number of arguments");
        }
    }

}
