package ru.service;

import org.springframework.stereotype.Service;
import ru.generated.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class NumberConverterService {
    private final NumberConversionSoapType numberConversionSoap;

    public NumberConverterService(NumberConversion numberConversion) {
        this.numberConversionSoap = numberConversion.getNumberConversionSoap();
    }

    public String numberToDollars(BigDecimal number) {
        return numberConversionSoap.numberToDollars(number);
    }

    public String numberToWords(BigInteger number) {
        return numberConversionSoap.numberToWords(number);
    }
}
