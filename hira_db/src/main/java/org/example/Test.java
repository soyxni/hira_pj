package org.example;

public class Test {

    static int i=0;
    static {i++;} //정적 초기화 문

    public static void main(String[] args){
//        String s="hello";
//        String s1 = "Mr.kim";
//        System.out.println(s+=s1);
//        String s = null;
//        String s1 = new String("hello");
//        String s2 = "hello";
//        System.out.println(s == s1); //address 비교
//        System.out.println(s.equals(s1)); //값 비교
//        System.out.println(s == s2);
        System.out.println(i);
    }


}
