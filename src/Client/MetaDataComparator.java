package Client;

import java.util.Comparator;

public class MetaDataComparator implements Comparator<MetaData> {
    public  int compare(MetaData mt1, MetaData mt2) {
        // debug
        //System.out.println("MT1 FILEPATH " + mt1.getFilePath() + "TIME " + mt1.getModified());
        //System.out.println("MT2 FILEPATH " + mt2.getFilePath() + "TIME " + mt2.getModified());
        if (mt1.getFilePath().equals(mt2.getFilePath()) )
            if (mt1.getModified() > mt2.getModified()){
                // debug
                //System.out.println("Res : " + 1);
                return 1;
            }
            else {
                // debug
                //System.out.println("Res : 0");
                return 0;
            }
        else {
            // debug
            //System.out.println("Res : " + -1);
            return -1;
        }



}
}
