package com.charles.schwab.url.shortner.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62CodecTest {

    @Test
    void testEncodeZero() {
        assertEquals("0", Base62Codec.encode(0));
    }

    @Test
    void testEncodePositiveNumber() {
        assertEquals("1", Base62Codec.encode(1));
        assertEquals("A", Base62Codec.encode(10));
        assertEquals("Z", Base62Codec.encode(35));
        assertEquals("a", Base62Codec.encode(36));
        assertEquals("z", Base62Codec.encode(61));
        assertEquals("10", Base62Codec.encode(62));
    }

    @Test
    void testEncodeMaxLong() {
        assertEquals("AzL8n0Y58m7", Base62Codec.encode(Long.MAX_VALUE));
    }

    @Test
    void testEncodeNegativeNumberThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Base62Codec.encode(-1);
        });
        assertEquals("ID must be non-negative", exception.getMessage());
    }

    @Test
    void testDecodeValidString() {
        assertEquals(0, Base62Codec.decode("0"));
        assertEquals(1, Base62Codec.decode("1"));
        assertEquals(10, Base62Codec.decode("A"));
        assertEquals(61, Base62Codec.decode("z"));
        assertEquals(62, Base62Codec.decode("10"));
        assertEquals(Long.MAX_VALUE, Base62Codec.decode("AzL8n0Y58m7"));
    }

    @Test
    void testDecodeInvalidCharacterThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Base62Codec.decode("1-a");
        });
        assertTrue(exception.getMessage().contains("Invalid character"));
    }

    @Test
    void testDecodeEmptyStringThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.decode(""));
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.decode(null));
    }
}
