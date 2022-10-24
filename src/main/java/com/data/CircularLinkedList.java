package com.data;

import com.Constants;

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
    private int size = 0;

    // this function will add the new node at the end of the list.
    public void add(T data) {
        // create new node
        Node<T> newNode = new Node<>(data);

        if (size == Constants.HISTORY_SIZE) {
            // set the new head pointer
            head = head.next;
            setTail(newNode);
            return;
        }

        // checks if the list is empty.
        if (head == null) {
            // if list is empty, head, prev and tail would point to new node.
            head = newNode;
            tail = newNode;
            newNode.next = head;
        } else {
            setTail(newNode);
        }
        size++;
    }

    public T back() {
        if (currNode == null) {
            currNode = this.tail;
            return (currNode != null ? currNode.data : null);
        }

        currNode = currNode.prev;
        return (currNode != null ? currNode.data : null);
    }

    public T forward() {
        if (currNode == null) {
            currNode = this.head;
        } else {
            currNode = currNode.next;
        }
        return (currNode != null ? currNode.data : null);
    }

    public boolean isEmpty() {
        return head == tail;
    }

    private void setTail(Node<T> newNode) {
        // tail will point to new node.
        tail.next = newNode;
        // hold a temp reference to current tail node
        var temp = tail;
        // new node will become new tail.
        tail = newNode;
        // circular tail will point to new head.
        tail.next = head;
        // link to previous tail node
        tail.prev = temp;
        // circular double linked
        head.prev = tail;
    }

}
