package orgp;


public class SearchTypeFind {

    public static void main(String[] args) {
        String term = "Little Rock, Riverfront Park";

        findMaster(term).printAttributes();

    }

    public static SearchItem findMaster(String seachTerm){

        String[] searchTermArray = seachTerm.split(",| ");

        try {
            SearchSingleTerm fullTerm = new SearchSingleTerm(seachTerm);
            //if the term matches 100% or the term can't be split into multiple characters then split
            if (!fullTerm.isEmpty() ){
                System.out.println("single term searched");
                return fullTerm.getTopResult();
            } else {
                //search for dead terms in the list so they do not kill the total search
                System.out.println("single term did not make the cut. it needs to be split up");

                if (seachTerm.contains(",")){
                    String[] commaSeperatedList = seachTerm.split(",");
                    SeachMultipleTermCollection commaBasedSearch = new SeachMultipleTermCollection(commaSeperatedList);

                    commaBasedSearch.printRecommendedResult();

                    return commaBasedSearch.getRecommendedResult();

                } else {
                    System.out.println("None of the conditions were met for an easy match. We are done");
                }

                //if the search by comma splitting fails then


            }
        } catch (Exception e){
            e.printStackTrace();
        }




        return null;
    }






}
