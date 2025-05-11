package pl.wachala.util;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class ArgumentsValidator {

    public void validateArgsNum(int argsNum) {
        if (argsNum != 2) {
            throw new RuntimeException("Invalid number of arguments - expected 2 arguments while: " + argsNum + " provided.");
        }
    }

}
