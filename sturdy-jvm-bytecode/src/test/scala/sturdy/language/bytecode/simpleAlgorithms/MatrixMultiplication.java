package sturdy.language.bytecode.simpleAlgorithms;
import java.util.Random;

public class MatrixMultiplication {
    public static void main(String[] args) {

        // Define dimensions for the matrices
        int rows1 = 3; // Rows of the first matrix
        int cols1 = 2; // Columns of the first matrix
        int rows2 = 2; // Rows of the second matrix
        int cols2 = 3; // Columns of the second matrix

        // Ensure matrices can be multiplied
        if (cols1 != rows2) {
            //System.out.println("Matrix multiplication is not possible. Columns of first matrix must equal rows of second matrix.");
            return;
        }

        // Initialize matrices with random values from 0 to 10
        //int[][] matrix1 = new int[rows1][cols1];
        //int[][] matrix2 = new int[rows2][cols2];
        int[][] result = new int[rows1][cols2];

        //System.out.println("Matrix 1:");
        int[][] matrix1 = {
                {2, 1},
                {1, 7},
                {4, 0}
        };

        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols1; j++) {
                //System.out.print(matrix1[i][j] + " ");
            }
            //System.out.println();
        }

        int[][] matrix2 = {
                {2, 0, 9},
                {3, 2, 7}
        };

        //System.out.println("\nMatrix 2:");
        for (int i = 0; i < rows2; i++) {
            for (int j = 0; j < cols2; j++) {
                //System.out.print(matrix2[i][j] + " ");
            }
            //System.out.println();
        }

        // Perform matrix multiplication
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                for (int k = 0; k < cols1; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }

        int sum = 0;
        // Display the result
        //System.out.println("\nResultant Matrix:");
        for (int i = 0; i < rows1; i++) {
            for (int j = 0; j < cols2; j++) {
                //System.out.print(result[i][j] + " ");
                sum += result[i][j];
            }
            //System.out.println();
        }
        /*
        7 2 25
        23 14 58
        8 0 36
        */
    }
}

