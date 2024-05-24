import java.util.ArrayList;
import java.util.List;

public class FindRepeatedElement {
    public static void FindRepeatedElement(int[] A, int index, List<Integer> duplicates) {
        if (index == A.length) {
            return;
        }

        int currentElement = A[index];

        // Check if the current element is a duplicate
        if (countOccurrences(A, currentElement, index) > 1 && !duplicates.contains(currentElement)) {
            duplicates.add(currentElement);
        }

        // Recurse on the next index (without considering the current element)
        FindRepeatedElement(A, index + 1, duplicates);
    }

    public static int countOccurrences(int[] A, int element, int index) {
        if (index == A.length) {
            return 0;
        }

        int count = countOccurrences(A, element, index + 1);

        if (A[index] == element) {
            count++;
        }

        return count;
    }

    public static void main(String[] args) {
        int[] inputArray = {1,1,5,7,8,9,5,3,2,5,7,2,1,1};
        List<Integer> duplicates = new ArrayList<>();
        FindRepeatedElement(inputArray, 0, duplicates);
        System.out.println(duplicates); // Output: [4, 3]
    }
}



