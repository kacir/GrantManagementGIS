package orgp;

import java.util.Comparator;

public class SearchItem implements Comparable<SearchItem>{

    private String identifier;
    private String type;
    private  Double score;
    private boolean strictMatch;
    private int typeRank = 0;
    private int hitCount = 1;
    private Double mergedScore;
    private Boolean mergedConflicts;

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
            case "park" :
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
            case "project":
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

    public void setMergedScore(Double score){
        this.mergedScore = score;
    }

    public SearchItem deepCopy(){
        try {
            SearchItem temp = new SearchItem(this.identifier, this.type, this.strictMatch, score);
            temp.mergedScore = this.mergedScore;
            return temp;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public void printAttributes(){
        System.out.println("Object " + this.identifier);
        System.out.println("table source " + this.type);
        System.out.println("score " + this.score);
        System.out.println("Strict: " + this.strictMatch);
    }

    @Override
    public int compareTo(SearchItem anotherResult){
        if (this.mergedScore > anotherResult.getScore()){
            return 1;
        } else if (this.mergedScore < anotherResult.getScore()){
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
