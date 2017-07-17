package utils

import groovy.xml.MarkupBuilder
import org.junit.Test

import static junit.framework.Assert.assertEquals

/**
 * Created by jacky on 16/7/12.
 */
class GXMLTest {
    @Test
    void test1() {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        def abc = 123

        xml.records() {
            car(name: 'HSV Maloo', make: 'Holden', year: 2006) {
                country('Australia')
                record(type: 'speed', 'Production Pickup Truck with speed of 271kph')
                record(type: 'speed', 'Production Pickup Truck with speed of 272kph' + abc)
            }
            car(name: 'Royale', make: 'Bugatti', year: 1931) {
                country('France')
                record(type: 'price', 'Most Valuable Car at $15 million')
            }
        }

        def expectedXMLStr = '''<records>
  <car name='HSV Maloo' make='Holden' year='2006'>
    <country>Australia</country>
    <record type='speed'>Production Pickup Truck with speed of 271kph</record>
    <record type='speed'>Production Pickup Truck with speed of 272kph123</record>
  </car>
  <car name='Royale' make='Bugatti' year='1931'>
    <country>France</country>
    <record type='price'>Most Valuable Car at $15 million</record>
  </car>
</records>'''

        assertEquals expectedXMLStr, writer.toString()


        def records = new XmlSlurper().parseText(writer.toString())

        assert records.car.first().@make.text() == 'Holden'
        assert records.car.first().name() == 'car'
        assert 'Royale' == records.car.last().@name.text()
    }
}
