package pl.wachala.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentsValidatorTest {

    private ArgumentsValidator validator = new ArgumentsValidator();

    @Test
    public void ShouldThrowRuntimeExceptionWhenDataIsNotConsistent() {
        //given
        int argsNum = 1;

        //when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> validator.validateArgsNum(argsNum));
    }

    @Test
    public void ShouldNotThrowExceptionWhenDataIsCorrect() {
        //given
        int argsNum = 2;

        //when & then
        assertDoesNotThrow(() -> validator.validateArgsNum(argsNum));
    }

    @Test
    public void ShouldThrowRuntimeExceptionWhenDataIsTooMany() {
        //given
        int argsNum = 56;

        //when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            validator.validateArgsNum(argsNum);
        });
    }
}
