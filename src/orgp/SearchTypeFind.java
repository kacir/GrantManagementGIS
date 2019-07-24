package orgp;


public class SearchTypeFind {

    public static void main(String[] args) {
        String term = "Gulley Park";

    }

    public static SearchItem findMaster(String seachTerm){

        String[] searchTermArray = seachTerm.split(",| ");

        try {
            SearchSingleTerm fullTerm = new SearchSingleTerm(seachTerm);
            //if the term matches 100% or the term can't be split into multiple characters then split
            if (!fullTerm.isEmpty() || searchTermArray.length == 1){
                return fullTerm.getTopResult();
            } else {
                //seach for dead terms in the list so they do not kill the total search


                if (seachTerm.contains(",")){
                    String[] commaSeperatedList = seachTerm.split(",");
                    SearchSingleTerm[] commaSearch = new SearchSingleTerm [commaSeperatedList.length];
                    Boolean searchComplete = true;
                    for (var i =0; i < commaSearch.length; i++){
                        commaSearch[i] = new SearchSingleTerm(commaSeperatedList[i]);
                        if (!commaSearch[i].matchContains100Percent()){
                            searchComplete = false;
                        }
                    }
                    if (searchComplete){
                        //find out which items are which in the search
                    }
                }

                //if the seach by comma splitting fails then


            }
        } catch (Exception e){
            e.printStackTrace();
        }




        return null;
    }






}
