package ast;

// Базовый класс чтобы парсер оптимизатор и генераторы работали с деревом единым способом
public abstract class Node {

    // текстовое представление дерева
    public abstract String print();
}
