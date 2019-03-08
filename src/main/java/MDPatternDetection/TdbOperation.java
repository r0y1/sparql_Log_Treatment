package MDPatternDetection;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static MDfromLogQueries.Declarations.Declarations.originalTdbDirectory;
import static MDfromLogQueries.Declarations.Declarations.tdbDirectory;


public class TdbOperation {
    private static Dataset dataset = TDBFactory.createDataset(tdbDirectory);
    private static Dataset originalDataDet = TDBFactory.createDataset(originalTdbDirectory);

    public static void main(String... argv) {

    }


    public static boolean exists(String name, Dataset dt) {

        boolean exists = false;
        // if exists a model with subject.toString == name
        if (dt.containsNamedModel(name)) exists = true;
        else {
            // Verify if it exists as a node inside some model in the tdb
            Iterator<String> it = dt.listNames();
            String subject;

            while (it.hasNext()) {
                subject = it.next();
                Model model = dt.getNamedModel(subject);

                if (model.containsResource(ResourceFactory.createResource(name))) exists = true;

            }
        }
        return exists;
    }


    public static void persistNonAnnotated(HashMap<String, Model> modelHashMap) {
        try {
            //Dataset dataset = DatasetFactory.create(model);

            Iterator it = modelHashMap.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry<String, Model> pair = (Map.Entry) it.next();

                if (exists(pair.getKey(), originalDataDet)) {
                    originalDataDet.getNamedModel(pair.getKey()).add(pair.getValue());
                } else {
                    originalDataDet.addNamedModel(pair.getKey(), pair.getValue());


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void persistAnnotatedHashMap(HashMap<String, Model> modelHashMap) {


        try {

            //;
            //Dataset dataset = DatasetFactory.create(model);

            Iterator it = modelHashMap.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry<String, Model> pair = (Map.Entry) it.next();

                //Verify if the model exists already in the tdb

                if (exists(pair.getKey(), dataset)) {
                    dataset.getNamedModel(pair.getKey()).add(pair.getValue());
                } else {
                    dataset.addNamedModel(pair.getKey(), pair.getValue());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*
    public Iterator<Triple> find (Node s, Node p, Node o)
    {
        Iterator<Tuple<NodeId>> iter = table.findAsNodeIds(s, p, o) ;
        if ( iter == null )
            return new NullIterator<>() ;
        Iterator<Triple> iter2 = TupleLib.convertToTriples(table.getNodeTable(), iter) ;
        return iter2 ;
    }
  */

    public static HashMap<String, Model> unpersistModelsMap() {
        HashMap<String, Model> results = new HashMap<>();

        //Dataset dataset = TDBFactory.createDataset(tdbDirectory);


        TDB.sync(dataset);


        // Model modell = dataset . getDefaultModel ();

        Iterator<String> it = dataset.listNames();
        String name;
        // it.next();

        try {
            while (it.hasNext()) {
                name = it.next();
                // Model model = dataset.getNamedModel(name);
                Model model = dataset.getNamedModel(name);

                StmtIterator stmtIterator = model.listStatements();

                while (stmtIterator.hasNext()) {
                    System.out.println(stmtIterator.next() + "\n*\n");
                }

                results.put(name, model);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

}