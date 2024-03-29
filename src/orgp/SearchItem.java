package orgp;

import java.awt.desktop.SystemEventListener;
import java.util.Comparator;

public class SearchItem implements Comparable<SearchItem>{

    private String originTerm;
    private String identifier;
    private String type;
    private String city;
    private String county;
    private  Double score;
    private boolean strictMatch;
    private int typeRank = 0;
    private int hitCount = 1;
    private Double mergedScore;
    private Boolean mergedConflicts;

    public static void main(String[] args){
        boolean temp = SearchItem.equalsLocation("mount ida", "mt. ida");
        System.out.println("result is " + temp);
    }

    public static String equalizeLocation(String location){
        //make sure it is comparable in length with another term that has different abbreviations in it.
        String result;

        if (location == null){
            return null;
        }

        result = location.replace("ST. ", "SAINT ")
                .replace("ST ", "SAINT ")
                .replace("MT ", "MOUNT ")
                .replace("MT. ", "MOUNT ");

        return result;
    }

    public static boolean equalsLocation (String a, String b){

        if (a == null || b == null){
            return false;
        }

        String aProcessed = a.trim().toUpperCase();
        String bProcessed = b.trim().toUpperCase();

        if (aProcessed.equals(bProcessed)){
            return true;
        }

        String[] comparableSaint = {"SAINT ", "ST. ", "ST "};

        for (String item : comparableSaint){
            if (aProcessed.contains(item)){
                aProcessed = aProcessed.replace(item , "SAINT ");
            }
            if (bProcessed.contains(item)){
                bProcessed = bProcessed.replace(item, "SAINT " );
            }
        }

        if (aProcessed.equals(bProcessed)){
            return true;
        }

        String[] comparableMount = {"MOUNT " , "MT ", "MT. " };
        for (String item : comparableMount){
            if (aProcessed.contains(item)){
                aProcessed = aProcessed.replace(item, "MOUNT ");
            }
            if (bProcessed.contains(item)){
                bProcessed = bProcessed.replace(item, "MOUNT ");
            }
        }
        if (bProcessed.equals(aProcessed)){
            return true;
        }

        //if either of the terms worked then it just does not match
        return false;
    }

    public void setMergedConflicts(Boolean temp){
        this.mergedConflicts = temp;
    }

    public static Comparator<SearchItem> arraySorter = new Comparator<SearchItem>(){
        @Override
        public int compare(SearchItem a , SearchItem b){
            return b.compareTo(a);
        }
    };

    public SearchItem (String identifier, String type, boolean strictMatch, Double score)throws Exception{

        if (score < 0 || score > 1){
            throw new Exception("score argument is outside of acceptable range for this application");
        }

        this.identifier = identifier;
        this.type = type;
        this.score = score;
        this.mergedScore = score;
        this.strictMatch = strictMatch;
        //give the type of result a type rank so if everything else is equal it can be sorted according to this list
        switch(type){
            case "City" :
                this.typeRank = 1;
                break;
            case "Park" :
                this.typeRank = 2;
                break;
            case "County" :
                this.typeRank = 3;
                break;
            case "State" :
                this.typeRank = 4;
                break;
            case "Community" :
                this.typeRank = 5;
                break;
            case "Project":
                this.typeRank = 7;
                break;

                default :
                    this.typeRank = 6;
        }
    }

    public Double getScore() {
        return this.score;
    }
    public String getIdentifier() {
        return this.identifier;
    }
    public String getType() {
        return this.type;
    }
    public boolean isStrictMatch() {
        return this.strictMatch;
    }
    public int getTypeRank(){
        return this.typeRank;
    }
    public Double getMergedScore(){
        return this.mergedScore;
    }
    public boolean originTermSame(SearchItem anotherObj){
        return SearchItem.equalsLocation(anotherObj.originTerm, this.originTerm);
    }
    public void setOriginTerm(String that){
        this.originTerm = that;
    }
    public void setCity(String city){
        this.city = city;
    }
    public String getCity(){
        return this.city;
    }
    public void setCounty(String county){
        this.county = county;
    }

    public String getCounty() {
        return county;
    }

    public void setMergedScore(Double score){
        this.mergedScore = score;
    }

    public SearchItem deepCopy(){
        try {
            SearchItem temp = new SearchItem(this.identifier, this.type, this.strictMatch, score);
            temp.mergedScore = this.mergedScore;
            temp.setOriginTerm(this.originTerm);
            temp.setCity(this.city);
            temp.setCounty(this.county);
            return temp;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void printAttributes(){
        System.out.println("Identifier " + this.identifier + "*********************************************");
        System.out.println("table source " + this.type);
        System.out.println("score " + this.score);
        System.out.println("Morged Score " + this.mergedScore);
        System.out.println("Strict: " + this.strictMatch);
        System.out.println("Conflicts " + this.mergedConflicts );
        System.out.println("County " + this.county);
        System.out.println("city " + this.city);
        System.out.println("origin term " + this.originTerm);
    }

    public boolean sameCity(SearchItem anotherObj){
        return SearchItem.equalsLocation(this.city , anotherObj.getCity());
    }
    public boolean sameCounty(SearchItem anotherObj){
        return SearchItem.equalsLocation(this.county, anotherObj.getCounty());
    }

    @Override
    public int compareTo(SearchItem anotherResult){
        if (this.score > anotherResult.getScore()){
            return 1;
        } else if (this.score < anotherResult.getScore()){
            return -1;
        } else {

            if (this.typeRank < anotherResult.getTypeRank()){
                return 1;
            } else if (this.typeRank > anotherResult.getTypeRank()){
                return -1;
            } else {
                //if the scores are the same then we can compare then based on the more strict search done.
                if (this.strictMatch == anotherResult.isStrictMatch()){
                    return 0;
                } else if (this.strictMatch){
                    return 1;
                } else if (anotherResult.isStrictMatch()){
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }
}
