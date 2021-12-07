package com.data;

public class CircularLinkedList<T> {

    public class Node<T> {
        final T data;
        Node next;
        Node prev;

        public Node(T data) {
            this.data = data;
        }
    }

    public Node head = null;
    public Node tail = null;
    public Node currNode = null;

    // this function will add the new node at the end of the list.
    public void add(T data) {
        // create new node
        var newNode = new Node(data);
        // checks if the list is empty.
        if (head == null) {
            // if list is empty, head, prev and tail would point to new node.
            head = newNode;
            tail = newNode;
            newNode.next = head;
        } else {
            // tail will point to new node.
            tail.next = newNode;
            // hold a temp reference to current tail node
            var temp = tail;
            // new node will become new tail.
            tail = newNode;
            // since, it is circular linked list tail will point to head.
            tail.next = head;
            // link to previous tail node
            tail.prev = temp;
            // circular double linked
            head.prev = tail;
        }
    }

    public T forward() {
        if (currNode == null) {
            currNode = this.head;
        } else {
            currNode = currNode.next;
        }
        return (T) currNode.data;
    }

    public T back() {
        T result;
        if (currNode == null) {
            currNode = this.tail;
            result = (T) currNode.data;
            return result;
        }

        currNode = currNode.prev;
        result = (T) currNode.data;
        return result;
    }

}
