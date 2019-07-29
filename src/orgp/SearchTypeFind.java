package orgp;

import java.util.ArrayList;
import java.util.Arrays;

public class SearchTypeFind {

    public static void main(String[] args) {
        String term = "Little Rock Riverfront Park";
        //findMaster(term).printAttributes();

        String[] termArray = term.split(" ");
        ArrayList<String[]> result = buildTermComboMaster(termArray);

        System.out.println("Completed");

    }

    public static ArrayList<String[]> buildTermComboMaster(String[] termArray){

        ArrayList<String[]> result = buildTermCombo(termArray);

        //remove redundant elements from array
        int resultCount = 0;
        while (resultCount < result.size()){
            String[] focusResult = result.get(resultCount);
            if (focusResult.length == termArray.length){
                result.remove(resultCount);
            } else {
                resultCount++;
            }
        }

        return result;
    }

    public static ArrayList<String[]> buildTermCombo(String[] termArray){
        ArrayList result = new ArrayList();

        //if the input array is not really configurable due to a lack of terms, then just return the object
        if (termArray.length == 0){
            return result;
        } else if (termArray.length == 1){
            result.add(termArray);
            return result;
        }

        //loop through each term in the array
        for (int a = 0; a < termArray.length; a++) {
            System.out.println("looping through combos starting with: '" + termArray[a] + "' combo elements are as follows................................................");
            String focusElement = termArray[a];

            String[] beforeElements = Arrays.copyOfRange(termArray, 0,a);
            System.out.println("Before Elements: " + String.join(", " , beforeElements));
            ArrayList<String[]> beforeCombos = buildTermCombo(beforeElements);

            String[] possibleFocusComboElements = Arrays.copyOfRange(termArray, a + 1 , termArray.length );

            System.out.println("combo: " + focusElement);
            System.out.println("      After Elements: " + String.join(", " , possibleFocusComboElements));


            //case when the focus term is not expanded further beyond the one focus word
            ArrayList<String[]> alphaBeforeCombo = buildTermCombo(beforeElements);
            ArrayList<String[]> alphaAfterCombo = buildTermCombo(possibleFocusComboElements);
            if (alphaAfterCombo.size() > 0 && alphaBeforeCombo.size() > 0){
                for (String[] beforeCombo : alphaBeforeCombo){
                    for (String[] afterCombo : alphaAfterCombo){
                        ArrayList<String> temp = new ArrayList<>();

                        for (String item : beforeCombo){
                            temp.add(item);
                        }
                        temp.add(focusElement);
                        for (String item : afterCombo){
                            temp.add(item);
                        }
                        String[] completedCombo = Arrays.copyOf(temp.toArray(), temp.size(), String[].class);
                        result.add(completedCombo);
                    }
                }
            } else if (alphaAfterCombo.size() > 0) {
                for (String[] afterCombo : alphaAfterCombo ){
                    ArrayList<String> temp = new ArrayList<>();

                    temp.add(focusElement);
                    for (String item : afterCombo){
                        temp.add(item);
                    }
                    String[] completedCombo = Arrays.copyOf(temp.toArray(), temp.size(), String[].class);
                    result.add(completedCombo);
                }
            } else if (alphaBeforeCombo.size() > 0){
                for (String[] beforeCombo : beforeCombos){
                    ArrayList<String> temp = new ArrayList<>();

                    for (String item : beforeCombo){
                        temp.add(item);
                    }
                    temp.add(focusElement);

                    String[] completedCombo = Arrays.copyOf(temp.toArray(), temp.size(), String[].class);
                    result.add(completedCombo);
                }
            }


            //loop through length of the possibleFocus Elements
            for (int innerIndex = 0; innerIndex < possibleFocusComboElements.length; innerIndex++){
                String[] selectedComboElements = Arrays.copyOfRange(possibleFocusComboElements, 0, innerIndex + 1);
                String focusCombo = focusElement;
                for (String item : selectedComboElements){
                    focusCombo +=  " " + item;
                }
                System.out.println("combo: " + focusCombo);

                String[] afterElements = Arrays.copyOfRange(possibleFocusComboElements, innerIndex + 1, possibleFocusComboElements.length);
                System.out.println("      After Elements: " + String.join(", " , afterElements));
                ArrayList<String[]> afterCombos = buildTermCombo(afterElements);

                //combine before, focus, and after combos into appropriate elements
                if (afterCombos.size() > 0 && beforeCombos.size() > 0){
                    for (String[] beforeCombo : beforeCombos){
                        for (String[] afterCombo : afterCombos){

                            ArrayList<String> temp = new ArrayList<>();
                            for (String item : beforeCombo){
                                temp.add(item);
                            }
                            temp.add(focusCombo);
                            for (String item : afterCombo){
                                temp.add(item);
                            }
                            String[] completedCombo = Arrays.copyOf(temp.toArray(), temp.size(), String[].class);
                            result.add(completedCombo);
                        }
                    }
                } else if (afterCombos.size() > 0){
                    for (String[] afterCombo : afterCombos ){
                        ArrayList<String> temp = new ArrayList<>();

                        temp.add(focusCombo);
                        for (String item : afterCombo){
                            temp.add(item);
                        }
                        String[] completedCombo = Arrays.copyOf(temp.toArray(), temp.size(), String[].class);
                        result.add(completedCombo);
                    }
                } else if (beforeCombos.size() > 0){
                    for (String[] beforeCombo : beforeCombos){
                        ArrayList<String> temp = new ArrayList<>();

                        for (String item : beforeCombo){
                            temp.add(item);
                        }
                        temp.add(focusCombo);

                        String[] completedCombo = Arrays.copyOf(temp.toArray(), temp.size(), String[].class);
                        result.add(completedCombo);
                    }
                }
            }
        }
        return result;
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
                    String[] commaSeparatedList = seachTerm.split(",");
                    SeachMultipleTermCollection commaBasedSearch = new SeachMultipleTermCollection(commaSeparatedList);

                    commaBasedSearch.printRecommendedResult();

                    return commaBasedSearch.getRecommendedResult();

                } else if(seachTerm.contains(" ")){
                    //go through every possible combination of of the terms
                    //then pick whichever combo has the highest score

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
