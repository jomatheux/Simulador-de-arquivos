import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * FileSystemSimulator
 * * Simula um sistema de arquivos básico com suporte a Journaling.
 * Implementa operações de criar, copiar, mover, deletar e listar arquivos/diretórios.
 */
public class FileSystemSimulator {

    // --- CLASSES DE DADOS (ESTRUTURA) ---

    // Classe abstrata base para Arquivos e Diretórios
    abstract static class FSNode {
        protected String name;
        protected Directory parent;
        protected Date createdAt;

        public FSNode(String name, Directory parent) {
            this.name = name;
            this.parent = parent;
            this.createdAt = new Date();
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Directory getParent() { return parent; }
        public void setParent(Directory parent) { this.parent = parent; }
        
        public abstract FSNode copy(Directory newParent);
    }

    // Representa um Arquivo
    static class File extends FSNode {
        private String content;

        public File(String name, Directory parent, String content) {
            super(name, parent);
            this.content = content;
        }

        @Override
        public FSNode copy(Directory newParent) {
            return new File(this.name, newParent, this.content);
        }
    }

    // Representa um Diretório
    static class Directory extends FSNode {
        private List<FSNode> children;

        public Directory(String name, Directory parent) {
            super(name, parent);
            this.children = new ArrayList<>();
        }

        public void addChild(FSNode node) {
            children.add(node);
            node.setParent(this);
        }

        public void removeChild(FSNode node) {
            children.remove(node);
        }

        public List<FSNode> getChildren() {
            return children;
        }

        public FSNode getChildByName(String name) {
            for (FSNode node : children) {
                if (node.getName().equals(name)) {
                    return node;
                }
            }
            return null;
        }

        @Override
        public FSNode copy(Directory newParent) {
            Directory newDir = new Directory(this.name, newParent);
            for (FSNode child : this.children) {
                newDir.addChild(child.copy(newDir));
            }
            return newDir;
        }
    }

    // --- COMPONENTE DE JOURNALING ---

    static class Journal {
        // Simula a escrita em um log seguro (Write-Ahead Logging)
        public void logStart(String operation, String target) {
            System.out.println("[JOURNAL - WAL] START OP: " + operation + " em '" + target + "' - " + new Date());
        }

        public void logCommit(String operation) {
            System.out.println("[JOURNAL - WAL] COMMIT: " + operation + " com sucesso.");
        }

        public void logError(String operation, String error) {
            System.err.println("[JOURNAL - WAL] ERROR: Falha em " + operation + ". Detalhe: " + error);
        }
    }

    // --- NÚCLEO DO SISTEMA DE ARQUIVOS ---

    static class FileSystem {
        private Directory root;
        private Directory currentDirectory;
        private Journal journal;

        public FileSystem() {
            this.root = new Directory("/", null);
            this.currentDirectory = this.root;
            this.journal = new Journal();
        }

        public String getCurrentPath() {
            if (currentDirectory == root) return "/";
            
            StringBuilder path = new StringBuilder();
            Directory temp = currentDirectory;
            while (temp != null && temp != root) {
                path.insert(0, "/" + temp.getName());
                temp = temp.getParent();
            }
            return path.toString();
        }

        // Operação: Criar Diretório (mkdir)
        public void mkdir(String name) {
            journal.logStart("MKDIR", name);
            if (currentDirectory.getChildByName(name) != null) {
                journal.logError("MKDIR", "Diretório já existe.");
                return;
            }
            Directory newDir = new Directory(name, currentDirectory);
            currentDirectory.addChild(newDir);
            journal.logCommit("MKDIR");
        }

        // Operação: Criar Arquivo (touch)
        public void touch(String name, String content) {
            journal.logStart("CREATE_FILE", name);
            if (currentDirectory.getChildByName(name) != null) {
                journal.logError("CREATE_FILE", "Arquivo já existe.");
                return;
            }
            File newFile = new File(name, currentDirectory, content);
            currentDirectory.addChild(newFile);
            journal.logCommit("CREATE_FILE");
        }

        // Operação: Listar (ls)
        public void ls() {
            System.out.println("Conteúdo de " + getCurrentPath() + ":");
            List<FSNode> nodes = currentDirectory.getChildren();
            if (nodes.isEmpty()) {
                System.out.println("(vazio)");
            } else {
                for (FSNode node : nodes) {
                    String type = (node instanceof Directory) ? "[DIR]" : "[ARQ]";
                    System.out.println(type + "\t" + node.getName());
                }
            }
        }

        // Operação: Mudar Diretório (cd)
        public void cd(String name) {
            if (name.equals("..")) {
                if (currentDirectory.getParent() != null) {
                    currentDirectory = currentDirectory.getParent();
                }
            } else if (name.equals("/")) {
                currentDirectory = root;
            } else {
                FSNode target = currentDirectory.getChildByName(name);
                if (target instanceof Directory) {
                    currentDirectory = (Directory) target;
                } else {
                    System.out.println("Erro: Diretório não encontrado ou é um arquivo.");
                }
            }
        }

        // Operação: Apagar (rm)
        public void rm(String name) {
            journal.logStart("DELETE", name);
            FSNode target = currentDirectory.getChildByName(name);
            if (target != null) {
                currentDirectory.removeChild(target);
                journal.logCommit("DELETE");
            } else {
                journal.logError("DELETE", "Alvo não encontrado.");
            }
        }

        // Operação: Renomear (mv / rename)
        public void rename(String oldName, String newName) {
            journal.logStart("RENAME", oldName + " -> " + newName);
            FSNode target = currentDirectory.getChildByName(oldName);
            if (target != null) {
                if (currentDirectory.getChildByName(newName) == null) {
                    target.setName(newName);
                    journal.logCommit("RENAME");
                } else {
                    journal.logError("RENAME", "Nome de destino já existe.");
                }
            } else {
                journal.logError("RENAME", "Alvo não encontrado.");
            }
        }

        // Operação: Copiar (cp)
        public void cp(String sourceName, String destName) {
            journal.logStart("COPY", sourceName + " -> " + destName);
            FSNode source = currentDirectory.getChildByName(sourceName);
            
            if (source != null) {
                if (currentDirectory.getChildByName(destName) == null) {
                    // Realiza a cópia profunda
                    FSNode copyNode = source.copy(currentDirectory);
                    copyNode.setName(destName);
                    currentDirectory.addChild(copyNode);
                    journal.logCommit("COPY");
                } else {
                    journal.logError("COPY", "Destino já existe.");
                }
            } else {
                journal.logError("COPY", "Fonte não encontrada.");
            }
        }
    }

    // --- MODO SHELL (INTERFACE) ---

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileSystem fs = new FileSystem();

        System.out.println("=== SIMULADOR DE SISTEMA DE ARQUIVOS JAVA (JOURNALING) ===");
        System.out.println("Comandos: ls, mkdir <nome>, touch <nome>, rm <nome>, cp <origem> <dest>, ren <old> <new>, cd <dir>, exit");
        
        while (true) {
            System.out.print("\nusuario@java-fs:" + fs.getCurrentPath() + "$ ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "exit":
                        System.out.println("Desligando sistema...");
                        scanner.close();
                        return;
                    case "ls":
                        fs.ls();
                        break;
                    case "mkdir":
                        if (parts.length > 1) fs.mkdir(parts[1]);
                        else System.out.println("Uso: mkdir <nome_diretorio>");
                        break;
                    case "touch":
                        if (parts.length > 1) fs.touch(parts[1], "conteudo_vazio");
                        else System.out.println("Uso: touch <nome_arquivo>");
                        break;
                    case "rm":
                        if (parts.length > 1) fs.rm(parts[1]);
                        else System.out.println("Uso: rm <nome>");
                        break;
                    case "cd":
                        if (parts.length > 1) fs.cd(parts[1]);
                        else System.out.println("Uso: cd <nome_diretorio> ou cd ..");
                        break;
                    case "ren":
                        if (parts.length > 2) fs.rename(parts[1], parts[2]);
                        else System.out.println("Uso: ren <nome_antigo> <nome_novo>");
                        break;
                    case "cp":
                        if (parts.length > 2) fs.cp(parts[1], parts[2]);
                        else System.out.println("Uso: cp <nome_origem> <nome_novo_clone>");
                        break;
                    default:
                        if (!command.isEmpty()) System.out.println("Comando desconhecido.");
                }
            } catch (Exception e) {
                System.out.println("Erro ao executar comando: " + e.getMessage());
            }
        }
    }
}