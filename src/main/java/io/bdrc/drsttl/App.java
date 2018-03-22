package io.bdrc.drsttl;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.sparql.util.Context;

import io.bdrc.ewtsconverter.EwtsConverter;
import io.bdrc.ewtsconverter.EwtsConverter.Mode;
import io.bdrc.jena.sttl.CompareComplex;
import io.bdrc.jena.sttl.ComparePredicates;
import io.bdrc.jena.sttl.STTLWriter;

/**
 * Hello world!
 *
 */
public class App 
{
    
    static final EwtsConverter ewtsC = new EwtsConverter(false, false, false, true, Mode.EWTS);
    static final EwtsConverter alalcC = new EwtsConverter(false, false, false, true, Mode.ALALC);
    static final PrefixMap pm = PrefixMapFactory.create();
    public static Lang sttl;
    public static Context ctx;
    static {
        pm.add("", "http://purl.bdrc.io/ontology/core/");
        pm.add("adm", "http://purl.bdrc.io/ontology/admin/");
        pm.add("bdr", "http://purl.bdrc.io/resource/");
        pm.add("tbr", "http://purl.bdrc.io/ontology/toberemoved/");
        pm.add("owl", "http://www.w3.org/2002/07/owl#");
        pm.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        pm.add("rdfs", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        pm.add("skos", "http://www.w3.org/2004/02/skos/core#");
        pm.add("vcard", "http://www.w3.org/2006/vcard/ns#");
        pm.add("xsd", "http://www.w3.org/2001/XMLSchema#");
        setupSTTL();
    }
    
    public static void setupSTTL() {
        sttl = STTLWriter.registerWriter();
        SortedMap<String, Integer> nsPrio = ComparePredicates.getDefaultNSPriorities();
        nsPrio.put(SKOS.getURI(), 1);
        nsPrio.put("http://purl.bdrc.io/ontology/admin/", 5);
        nsPrio.put("http://purl.bdrc.io/ontology/toberemoved/", 6);
        List<String> predicatesPrio = CompareComplex.getDefaultPropUris();
        predicatesPrio.add("http://purl.bdrc.io/ontology/admin/logDate");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/seqNum");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/onOrAbout");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/noteText");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/noteWork");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/noteLocationStatement");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/volumeNumber");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/workSitePlace");
        predicatesPrio.add("http://purl.bdrc.io/ontology/core/lineageWho");
        ctx = new Context();
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "nsPriorities"), nsPrio);
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "nsDefaultPriority"), 2);
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "complexPredicatesPriorities"), predicatesPrio);
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "indentBase"), 4);
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "predicateBaseWidth"), 18);
    }
    
    public static void addPrefixes(final Model m) {
        m.setNsPrefix("", "http://purl.bdrc.io/ontology/core/");
        m.setNsPrefix("adm", "http://purl.bdrc.io/ontology/admin/");
        m.setNsPrefix("bdr", "http://purl.bdrc.io/resource/");
        m.setNsPrefix("tbr", "http://purl.bdrc.io/ontology/toberemoved/");
        m.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        m.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.setNsPrefix("rdfs", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        m.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        m.setNsPrefix("vcard", "http://www.w3.org/2006/vcard/ns#");
        m.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
    }
    
    public static void main( String[] args ) 
    {
        final Model model = RDFDataMgr.loadModel("/home/eroux/BUDA/softs/xmltoldmigration/tbrc-ttl/works/aa/W23430.ttl");
        addPrefixes(model);
        final List<Statement> newStatements = new ArrayList<>();
        final StmtIterator it =  model.listStatements();
        while (it.hasNext()) {
             final Statement stmt = it.next();
             try {
                 String lang = stmt.getLanguage();
                 String resStr = null;
                 if (lang.equals("bo-x-ewts")) {
                     resStr = ewtsC.toUnicode(stmt.getString(), null, true);
                 } else if (lang.equals("bo-alalc97")) {
                     resStr = alalcC.toUnicode(stmt.getString(), null, true);
                 } else {
                     continue;
                 }
                 if (resStr.isEmpty()) continue;
                 // createStatement does not add the statement to the model
                 final Statement newStatement = model.createStatement(stmt.getResource(), stmt.getPredicate(), resStr, "bo");
                 newStatements.add(newStatement);
             } catch (Exception e) {
                 //System.out.println(e.getMessage());
                 continue;
             }
        }
        //System.out.println("add "+newStatements.size()+" statements");
        model.add(newStatements);
        RDFWriter.create().source(model.getGraph()).context(ctx).lang(sttl).build().output(System.out);
	}

}
