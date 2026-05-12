1. Functional Requirements

Multiple users can edit the same document in real-time.
Support Insert and Delete operations.
Operational Transformation (OT) to resolve conflicts when users edit simultaneously.
Maintain document consistency across all users.
Track operation history for transformation.
Show basic user activity.

2. Non-Functional Requirements

Eventual consistency with low latency.
Conflict-free collaborative editing using OT.
Efficient operation handling.
In-memory implementation.

3. System Happy Flow

User creates/opens a document.
Multiple users join the document.
A user performs an edit → Operation is created.
DocumentService transforms the operation against all previous operations.
Transformed operation is applied to the document.
Updated content is broadcast to all users.

4. Edge Cases + Handling

Two users editing same position → OT resolves conflict.
Late-joining user → Gets latest document state.
Operations arriving out of order → Version-based transformation.
Delete on non-existing text → Safe handling.


5. UML Diagram

@startuml
skinparam classAttributeIconSize 0

enum OperationType {
  INSERT, DELETE
  '' Type of edit operation
}

class User {
  - String userId
  - String name
  - int cursorPosition
  + User(String userId, String name)
  '' Active collaborator
}

class Operation {
  - String operationId
  - OperationType type
  - int position
  - String text
  - String userId
  - int version
  + Operation(OperationType type, int position, String text, String userId, int version)
  '' Single atomic edit operation
}

class Document {
  - String documentId
  - StringBuilder content
  - int version
  - List<Operation> history
  + Document(String documentId)
  + void applyOperation(Operation op)
  + String getContent()
  + int getVersion()
  + List<Operation> getHistory()
  '' Core document with content and full history
}

class OperationalTransformer {
  + Operation transform(Operation newOp, Operation oldOp)
  '' Applies Operational Transformation to resolve conflicts
}

class DocumentService {
  - Map<String, Document> documents
  - Map<String, List<User>> activeUsers
  - OperationalTransformer transformer
  + DocumentService()
  + Document createDocument(String documentId)
  + void joinDocument(String documentId, User user)
  + void editDocument(String documentId, Operation operation)  // Applies OT before applying
  + String getDocumentContent(String documentId)
  '' Main service managing documents and collaboration
}

User "1" --> DocumentService
DocumentService "1" *-- "many" Document
Document "1" *-- "many" Operation : history
DocumentService --> OperationalTransformer
Operation --> OperationType
@enduml




import java.util.*;

// OperationType.java
public enum OperationType {
    INSERT, DELETE
}

// User.java
public class User {
    private final String userId;
    private final String name;
    private int cursorPosition;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.cursorPosition = 0;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
}

// Operation.java
public class Operation {
    private final String operationId;
    private final OperationType type;
    private final int position;
    private final String text;
    private final String userId;
    private final int version;

    public Operation(OperationType type, int position, String text, String userId, int version) {
        this.operationId = "OP-" + System.currentTimeMillis();
        this.type = type;
        this.position = position;
        this.text = text;
        this.userId = userId;
        this.version = version;
    }

    public OperationType getType() { return type; }
    public int getPosition() { return position; }
    public String getText() { return text; }
    public String getUserId() { return userId; }
    public int getVersion() { return version; }
}

// OperationalTransformer.java
public class OperationalTransformer {

    public Operation transform(Operation newOp, Operation oldOp) {
        if (newOp.getPosition() < oldOp.getPosition()) {
            return newOp; // No transformation needed
        }

        int shift = 0;
        if (oldOp.getType() == OperationType.INSERT) {
            shift = oldOp.getText().length();
        } else if (oldOp.getType() == OperationType.DELETE) {
            shift = -oldOp.getText().length();
        }

        int newPosition = newOp.getPosition() + shift;

        return new Operation(
            newOp.getType(),
            newPosition,
            newOp.getText(),
            newOp.getUserId(),
            newOp.getVersion()
        );
    }
}

// Document.java
public class Document {
    private final String documentId;
    private final StringBuilder content;
    private int version;
    private final List<Operation> history;

    public Document(String documentId) {
        this.documentId = documentId;
        this.content = new StringBuilder();
        this.version = 0;
        this.history = new ArrayList<>();
    }

    public void applyOperation(Operation op) {
        if (op.getType() == OperationType.INSERT) {
            content.insert(op.getPosition(), op.getText());
        } else if (op.getType() == OperationType.DELETE) {
            int len = Math.min(op.getText().length(), content.length() - op.getPosition());
            if (len > 0) {
                content.delete(op.getPosition(), op.getPosition() + len);
            }
        }
        history.add(op);
        version++;
    }

    public String getContent() {
        return content.toString();
    }

    public int getVersion() { return version; }
    public List<Operation> getHistory() { return history; }
}

// DocumentService.java
public class DocumentService {
    private final Map<String, Document> documents = new HashMap<>();
    private final Map<String, List<User>> activeUsers = new HashMap<>();
    private final OperationalTransformer transformer = new OperationalTransformer();

    public Document createDocument(String documentId) {
        Document doc = new Document(documentId);
        documents.put(documentId, doc);
        activeUsers.put(documentId, new ArrayList<>());
        return doc;
    }

    public void joinDocument(String documentId, User user) {
        activeUsers.get(documentId).add(user);
        System.out.println(user.getName() + " joined document: " + documentId);
    }

    public void editDocument(String documentId, Operation newOperation) {
        Document doc = documents.get(documentId);
        if (doc == null) return;

        List<Operation> history = doc.getHistory();

        Operation transformedOp = newOperation;

        // Transform new operation against all previous operations
        for (int i = 0; i < history.size(); i++) {
            Operation oldOp = history.get(i);
            if (oldOp.getVersion() >= transformedOp.getVersion()) {
                transformedOp = transformer.transform(transformedOp, oldOp);
            }
        }

        doc.applyOperation(transformedOp);
        
        System.out.println("User " + transformedOp.getUserId() + " performed " + 
                          transformedOp.getType() + " at position " + transformedOp.getPosition());
    }

    public String getDocumentContent(String documentId) {
        Document doc = documents.get(documentId);
        return doc != null ? doc.getContent() : "";
    }
}

// Demo
public class GoogleDocsDemo {
    public static void main(String[] args) {
        DocumentService service = new DocumentService();
        Document doc = service.createDocument("doc-google-123");

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");

        service.joinDocument("doc-google-123", alice);
        service.joinDocument("doc-google-123", bob);

        // Alice types "Hello World"
        Operation op1 = new Operation(OperationType.INSERT, 0, "Hello World", "U1", 1);
        service.editDocument("doc-google-123", op1);

        // Bob types " Everyone" at position 5 (concurrent edit)
        Operation op2 = new Operation(OperationType.INSERT, 5, " Everyone", "U2", 2);
        service.editDocument("doc-google-123", op2);

        System.out.println("\nFinal Document: " + service.getDocumentContent("doc-google-123"));
    }
}