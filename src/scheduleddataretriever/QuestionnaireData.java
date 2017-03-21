/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import static scheduleddataretriever.Utilities.sendGET;

/**
 *
 * @author Paris
 */
public class QuestionnaireData {
    private String UUID;
    
    public QuestionnaireData(String UUID){
        this.UUID = UUID;
    }
    
    public void getAbnormalQuestionnaireAnswers(long yesterdayMorningMillis) throws IOException{
        InputStream questionnaires = this.getQuestionnaires(UUID, yesterdayMorningMillis);
        this.parseQuestionnaires(questionnaires);
    }
    
    InputStream getQuestionnaires(String UUID, long start) throws IOException, FileNotFoundException {
//        InputStream is = new FileInputStream("data.ttl");
//        return is;
        Calendar moment = Calendar.getInstance();
        moment.setTimeInMillis(start);
        String year = String.valueOf(moment.get(Calendar.YEAR));
        String month = String.valueOf(moment.get(Calendar.MONTH) + 1);
        month = "0" + month; // add "0" (if 1 digit => 0x) and take 2 last digits of string.
        month = month.substring(month.length() - 2, month.length());
        String day = String.valueOf(moment.get(Calendar.DAY_OF_MONTH));
        day = "0" + day;
        day = day.substring(day.length() - 2, day.length());
        String startString = year + "-" + month + "-" + day;

        String URL = "http://inlife-1.inab.certh.gr:8080/inlife/api/data/Patient/"
                + UUID
                + "/QuestionnaireAnswers?q=QuestionnaireAnswers.authored,afterEq,"
                + startString
                + "&limit=999";
        InputStream response = sendGET(URL, true);
        return response;

    }
    
    public void parseQuestionnaires(InputStream questionnaires) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(questionnaires, null, "TURTLE");
//        model.write(System.out, "TURTLE");

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement st = iter.next();
            if (!st.getPredicate().equals(RDF.type)) {

                String uri = st.getObject().asResource().getURI();
//                System.out.println("asodjhsajjds    " + uri);
                InputStream singleQuestionnaire = sendGET(uri, true);
                Model newModel = ModelFactory.createDefaultModel();
                newModel.read(singleQuestionnaire, null, "TURTLE");
//                newModel.write(System.out, "TURTLE");
                String queryString = "SELECT (COUNT(?s) AS ?count) WHERE\n"
                        + "{\n"
                        + "?s <http://lomi.med.auth.gr/ontologies/FHIRResourcesExtensions#questionAnswer>/"
                        + "<http://lomi.med.auth.gr/ontologies/FHIRResourcesExtensions#QuestionAnswer.interpretation> "
                        + "<http://lomi.med.auth.gr/ontologies/FHIRResources#ObservationInterpretation_A>\n"
                        + "}";
                Query query = QueryFactory.create(queryString);
                QueryExecution qexec = QueryExecutionFactory.create(query, newModel);
                ResultSet result = qexec.execSelect();
                String resultString = result.next().getLiteral("count").getString();
                qexec.close();
                Integer abnormals = Integer.parseInt(resultString);
                System.out.println("abnormals " + abnormals);
//                
                String queryString2 = "SELECT (COUNT(?s) AS ?count) WHERE\n"
                        + "{\n"
                        + "?s <http://lomi.med.auth.gr/ontologies/FHIRResourcesExtensions#questionAnswer>/"
                        + "<http://lomi.med.auth.gr/ontologies/FHIRResourcesExtensions#QuestionAnswer.interpretation> "
                        + "<http://lomi.med.auth.gr/ontologies/FHIRResources#ObservationInterpretation_N>\n"
                        + "}";
                Query query2 = QueryFactory.create(queryString2);
                QueryExecution qexec2 = QueryExecutionFactory.create(query2, newModel);
                ResultSet result2 = qexec2.execSelect();
                String resultString2 = result2.next().getLiteral("count").getString();
                qexec2.close();
                int normals = Integer.parseInt(resultString2);
                System.out.println("normals " + normals);
            }
        }

    }
    
}
