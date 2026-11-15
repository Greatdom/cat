package com.wdd.cat.message;


/**
 * &#064program: cat
 * <p>
 * &#064description: 一个会话的标识，形如 "group:20001"
 * <p>
 * &#064author: black-cat
 * <p>
 * &#064create: 2026-11-15 21:24
 **/

public record ChatKey(ChatType type, String id) {
    public static ChatKey parse(String s){
        return null;//TODO
    }   // "group:20001" -> (GROUP, "20001")
    public String toString(){
        return null;//TODO
    }              // (GROUP,"20001") -> "group:20001"
    public enum ChatType { PRIVATE, GROUP }

}
