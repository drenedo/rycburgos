package me.renedo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Address {

    public final static String BLANK = "";
    public final static String SPACE = " ";

    private String text;

    public void clean(String regex){
        text = text.replaceAll(regex, SPACE);
    }

    public void cleanWord(String regex){
        text = text.replaceAll(SPACE+regex+SPACE, SPACE);
    }

    public void clean(String regex, String replacement){
        text = text.replaceAll(regex, replacement);
    }

    public static Address of(String text){
        return new Address(SPACE+text.toLowerCase());
    }

    public boolean contains(String s){
        return text.contains(s);
    }

    public void replace(String regex, String replacement){
        text = text.replaceAll(regex, replacement);
    }
}
