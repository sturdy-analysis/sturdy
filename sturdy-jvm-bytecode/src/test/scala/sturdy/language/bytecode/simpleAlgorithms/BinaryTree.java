package sturdy.language.bytecode.simpleAlgorithms;

// Node class to represent each node in the binary tree
class Node {
    int data;
    Node left, right;

    // Constructor
    public Node(int data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }
}

// Tree class to represent the binary tree
class Tree {
    Node root;

    // Constructor
    public Tree() {
        root = null;
    }

    // Method to insert a new node into the binary tree
    public void insert(int data) {
        root = insertRec(root, data);
    }

    // Recursive helper function to insert a node
    private Node insertRec(Node root, int data) {
        if (root == null) {
            root = new Node(data);
            return root;
        }
        if (data < root.data) {
            root.left = insertRec(root.left, data);
        } else if (data > root.data) {
            root.right = insertRec(root.right, data);
        }
        return root;
    }

    // Method for inorder traversal (left, root, right)
    public void inorder() {
        inorderRec(root);
    }

    // Recursive helper function for inorder traversal
    private void inorderRec(Node root) {
        if (root != null) {
            inorderRec(root.left);
            System.out.print(root.data + " ");
            inorderRec(root.right);
        }
    }

    // Method for preorder traversal (root, left, right)
    public void preorder() {
        preorderRec(root);
    }

    // Recursive helper function for preorder traversal
    private void preorderRec(Node root) {
        if (root != null) {
            System.out.print(root.data + " ");
            preorderRec(root.left);
            preorderRec(root.right);
        }
    }

    // Method for postorder traversal (left, right, root)
    public void postorder() {
        postorderRec(root);
    }

    // Recursive helper function for postorder traversal
    private void postorderRec(Node root) {
        if (root != null) {
            postorderRec(root.left);
            postorderRec(root.right);
            System.out.print(root.data + " ");
        }
    }
}

// Main class to test the binary tree implementation
public class BinaryTree {
    public static void main(String[] args) {
        Tree tree = new Tree();

        // Insert nodes into the binary tree
        tree.insert(50);
        tree.insert(30);
        tree.insert(70);
        tree.insert(20);
        tree.insert(40);
        tree.insert(60);
        tree.insert(80);

        // Display the tree in different traversals
        System.out.println("Inorder Traversal:");
        tree.inorder();
        System.out.println("\nPreorder Traversal:");
        tree.preorder();
        System.out.println("\nPostorder Traversal:");
        tree.postorder();
    }
}
