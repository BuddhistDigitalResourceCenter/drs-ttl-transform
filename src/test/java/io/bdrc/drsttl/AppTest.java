package io.bdrc.drsttl;

import static org.junit.Assert.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

public class AppTest {
    @Test
    public void testModel() {
        Model m = App.getBoModel("test.ttl");
        Model expected = ModelFactory.createDefaultModel();
        expected.read("expected.ttl");
        assertTrue(m.isIsomorphicWith(expected));
        //App.printModel(m, System.out);
    }
}
