package example.util;

import org.example.PaymentOptimizer;
import org.example.util.DataValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataValidatorTest {
    @Test
    public void ShouldThrowRuntimeExceptionWhenDataIsNotConsistent() {
        int argsNum = 1;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            DataValidator.ValidateArgsNum(argsNum);
        });

        assertEquals("Invalid number of arguments", exception.getMessage());
    }

    @Test
    public void ShouldNotThrowExceptionWhenDataIsCorrect() {
        int argsNum = 2;

        assertDoesNotThrow(() -> {
            DataValidator.ValidateArgsNum(argsNum);
        });
    }

    @Test
    public void ShouldThrowRuntimeExceptionWhenDataIsTooMany() {
        int argsNum = 3;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            DataValidator.ValidateArgsNum(argsNum);
        });

        assertEquals("Invalid number of arguments", exception.getMessage());
    }
}
