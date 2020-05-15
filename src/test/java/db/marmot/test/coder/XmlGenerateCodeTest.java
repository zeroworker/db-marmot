package db.marmot.test.coder;

import db.marmot.coder.XmlGenerateCoder;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.test.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shaokang
 */
public class XmlGenerateCodeTest extends TestBase {

    @Autowired
    private RepositoryAdapter repositoryAdapter;

	@Test
	public void volumeCoderTest() {
        XmlGenerateCoder xmlGenerateCoder = new XmlGenerateCoder(repositoryAdapter);
        xmlGenerateCoder.parseVolume("classpath:xsd/volume-test.xml");
	}
}
