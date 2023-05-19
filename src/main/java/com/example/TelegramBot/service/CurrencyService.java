package com.example.TelegramBot.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import com.example.TelegramBot.models.Currency;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


public class CurrencyService {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    LocalDateTime now = LocalDateTime.now();
    public String date = dtf.format(now).toString();
    public String URL = "https://nationalbank.kz/rss/get_rates.cfm?fdate="+date;
    public static Currency parseCurrencyElement(Element currencyElement) {
        Currency currency = new Currency();
        currency.setFullname(currencyElement.getChildText("fullname"));
        currency.setTitle(currencyElement.getChildText("title"));
        currency.setDescription(Double.parseDouble(currencyElement.getChildText("description")));
        currency.setQuant(Integer.parseInt(currencyElement.getChildText("quant")));
        return currency;
    }

    public static List<Currency> getCurrencies(String date) throws IOException {
        URL url = new URL("https://nationalbank.kz/rss/get_rates.cfm?fdate=" + date);
        Scanner scanner = new Scanner((InputStream) url.getContent());
        String result = "";
        while (scanner.hasNext()) {
            result += scanner.nextLine();
        }

        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document document = saxBuilder.build(new StringReader(result));
            Element rootElement = document.getRootElement();
            List<Element> itemElements = rootElement.getChildren("item");

            List<Currency> currencies = new ArrayList<>();
            for (Element itemElement : itemElements) {
                Currency currency = parseCurrencyElement(itemElement);
                currencies.add(currency);
            }
            return currencies;
        } catch (JDOMException e) {
            e.printStackTrace();
        }
        return null;
    }



}
