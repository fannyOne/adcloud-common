package com.asiainfo.comm.module.build.dao.impl

import spock.lang.*

class One {
    String message = "foo";

    public String foo() {
        return message;
    }

    public void uncoveredMethod() {
        System.out.println(foo());
    }
}

@Title("向指定账号存款")
@Narrative("""关于测试的大段文本描述""")
@Subject(One)
//标明被测试的类是One
public class MathTest extends Specification {
    def setupSpec() {
        //设置每个测试类的环境
    }

    def setup() {
        //设置每个测试方法的环境，每个测试方法执行一次
    }

    @Issue(["问题#23", "问题#34"])
    def "向A账号存入100元"() {

        given: "A账号余额10元"
        One one = new One()

        and: "其他前置条件"
        def a = 1

        when: "向A账号存入100元"
        def result = one.foo()

        then: "A账号余额为110元"
        a == 1
        result == "foo"
    }

    def "Stub and Mock"() {
        given: "arrange"
        def one = Mock(One) {
            foo() >> "3355"
        }

        when: "act"
        def result = one.foo()

        then: "assert"
        1 * one.foo()
    }


}