/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scheduleddataretriever;

/**
 *
 * @author Paris
 */
public class TurtleTemplate {

    public TurtleTemplate() {

    }
    
    public String prepareValueTemplate(String endPoint, String dateTime, String periodOfDay, String method, String value, String patient){
        System.out.println("value " + endPoint + " "+ dateTime + " "+ periodOfDay + " "+ method + " "+ value);
        String temp = TTL_TEMPLATE_VALUE.replaceAll("@@@dateTime", dateTime)
                .replaceAll("@@@periodOfDay", periodOfDay)
                .replaceAll("@@@method", method)
                .replaceAll("@@@value", value)
                .replaceAll("@@@patient", patient)
                .replaceAll("@@@endPoint", endPoint);
                
        return temp;
    }
    
    public String prepareRatioTemplate(String endPoint, String dateTime, String periodOfDay, String method, String numerator, String denominator, String patient){
        System.out.println("value " + endPoint + " "+ dateTime + " "+ periodOfDay + " "+ method + " "+ numerator + "/"+denominator);
        String temp = TTL_TEMPLATE_RATIO.replaceAll("@@@dateTime", dateTime)
                .replaceAll("@@@periodOfDay", periodOfDay)
                .replaceAll("@@@method", method)
                .replaceAll("@@@denominator", denominator)
                .replaceAll("@@@numerator", numerator)
                .replaceAll("@@@patient", patient)
                .replaceAll("@@@endPoint", endPoint);
                
        return temp;
    }

       
    
    private static final String TTL_TEMPLATE_RATIO = String.join("\n", 
            "@prefix ns6:   <http://lomi.med.auth.gr/ontologies/FHIRComplexTypes#> .",
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .",
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .",
            "@prefix ns2:   <http://lomi.med.auth.gr/ontologies/INLIFE_entities#> .",
            "@prefix ns1:   <inlife://inlife-project.eu/data/SleepQualityIndex/> .",
            "@prefix ns4:   <http://lomi.med.auth.gr/ontologies/FHIRPrimitiveTypes#> .",
            "@prefix ns3:   <http://lomi.med.auth.gr/ontologies/FHIRResources#> .",
            "[]",
                    "a                        ns2:@@@endPoint ;",
                    "ns3:Observation.applies  [ a          ns4:dateTime ;",
                                               "rdf:value  \"@@@dateTime\"^^xsd:gYearMonth",
                                             "] ;",
                    "ns3:Observation.method   ns3:@@@periodOfDay , ns3:@@@method ;",
                    "ns3:Observation.value    [ a                      ns6:Ratio ;",
                                               "ns6:Ratio.denominator  [ a                   ns6:Quantity ;",
                                                                        "ns6:Quantity.value  [ a          ns4:decimal ;",
                                                                                              "rdf:value  @@@denominator",
                                                                                            "]",
                                                                      "] ;",
                                               "ns6:Ratio.numerator    [ a                   ns6:Quantity ;",
                                                                        "ns6:Quantity.value  [ a          ns4:decimal ;",
                                                                                              "rdf:value  @@@numerator",
                                                                                            "]",
                                                                      "]",
                                             "] ;",
                    "ns3:device               <http://inlife-1.inab.certh.gr:8080/inlife/api/data/FeatureExtractionServer/88e8dd64-3255-4767-bff7-06d16f0d87c0> ;",
                    "ns3:subject              <http://inlife-1.inab.certh.gr:8080/inlife/api/data/Patient/@@@patient> ."
            );
    
    
    
    private static final String TTL_TEMPLATE_VALUE = String.join("\n", 
            "@prefix ns5:   <http://lomi.med.auth.gr/ontologies/FHIRComplexTypes#> .",
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .",
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .",
            "@prefix ns2:   <http://lomi.med.auth.gr/ontologies/FHIRResources#> .",
            "@prefix ns1:   <http://lomi.med.auth.gr/ontologies/INLIFE_entities#> .",
            "@prefix ns3:   <http://lomi.med.auth.gr/ontologies/FHIRPrimitiveTypes#> .",
            "[]",
                    "a                        ns1:@@@endPoint ;",
                    "ns2:Observation.applies  [ a          ns3:dateTime ;",
                                               "rdf:value  \"@@@dateTime\"^^xsd:gYearMonth",
                                             "] ;",
                    "ns2:Observation.method   ns2:@@@method , ns2:@@@periodOfDay ;",
                    "ns2:Observation.value    [ a                   ns5:Quantity ;",
                                               "ns5:Quantity.value  [ a          ns3:decimal ;",
                                                                     "rdf:value  @@@value",
                                                                  "]",
                                             "] ;",
                    "ns2:device               <http://inlife-1.inab.certh.gr:8080/inlife/api/data/FeatureExtractionServer/88e8dd64-3255-4767-bff7-06d16f0d87c0> ;",
                    "ns2:subject              <http://inlife-1.inab.certh.gr:8080/inlife/api/data/Patient/@@@patient> ."
            );
    

}
