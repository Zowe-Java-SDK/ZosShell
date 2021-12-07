package com.data;

public class CircularLinkedList<T> {

    public static class Node<T> {
        final T data;
        Node<T> next;
        Node<T> prev;

        public Node(T data) {
            this.data = data;
        }
    }

    public Node<T> head = null;
    public Node<T> tail = null;
    public Node<T> currNode = null;

    // this function will add the new node at the end of the list.
    public void add(T data) {
        // create new node
        Node<T> newNode = new Node<>(data);
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
        return currNode.data;
    }

    public T back() {
        T result;
        if (currNode == null) {
            currNode = this.tail;
            result = currNode.data;
            return result;
        }

        currNode = currNode.prev;
        result = currNode.data;
        return result;
    }

}
