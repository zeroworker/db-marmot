package db.marmot.test.coder;

import db.marmot.coder.XmlGenerateCoder;
import db.marmot.repository.DataSourceRepository;
import db.marmot.test.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shaokang
 */
public class XmlGenerateCodeTest extends TestBase {

    @Autowired
    private DataSourceRepository dataSourceRepository;

	@Test
	public void volumeCoderTest() {
        XmlGenerateCoder xmlGenerateCoder = new XmlGenerateCoder(dataSourceRepository);
        xmlGenerateCoder.parseVolume("classpath:xsd/volume-test.xml");
	}
}
