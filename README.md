# 📂 Simulador de Sistema de Arquivos com Journaling

> **Disciplina:** Sistemas Operacionais  
> **Linguagem:** Java  
> **Autor:** João Matheus Magalhães

---

## 📖 Sobre o Projeto

Este projeto consiste em um simulador desenvolvido na linguagem **Java**, adotando o paradigma de Orientação a Objetos para representar as entidades fundamentais de um sistema de arquivos (Arquivos e Diretórios).

O programa opera em **"Modo Shell"** (Linha de Comando), onde um loop infinito captura a entrada do usuário, processa a string de comando e invoca os métodos correspondentes na classe controladora.

### 🛡️ Diferencial: Journaling
Para garantir a integridade simulada, foi implementado um mecanismo de **Journaling** simples. Ele imprime logs no console antes (*Write-Ahead*) e depois da execução de cada operação crítica, simulando a segurança de dados em sistemas reais.

---

## 🧠 Conceitos Teóricos

### O Sistema de Arquivos
Um *File System* é o conjunto de estruturas lógicas e regras utilizadas pelo SO para controlar o armazenamento e recuperação de dados. Ele gerencia o espaço alocado, mantém a hierarquia de diretórios e armazena metadados (nome, permissões, data de criação).

### O Journaling
Técnica utilizada para garantir a consistência do sistema em caso de falhas (crashes). Antes de realizar alterações, o sistema grava as intenções em uma área chamada "Journal".

* **Write-Ahead Logging (WAL):** Método simulado neste projeto. Apenas os metadados são logados antes da escrita.
    1.  Registra "Vou criar o arquivo X".
    2.  Executa a ação.
    3.  Marca como "Concluído".

---

## 🏗️ Arquitetura do Simulador

### Estrutura de Dados (Árvore)
Para representar o sistema em memória, utilizamos uma estrutura de árvore (*Tree*):

* **`FSNode` (Nó Abstrato):** Classe base com atributos comuns (nome, pai, data de criação).
* **`Directory` (Diretório):** Estende `FSNode`. Possui uma lista de filhos (`children`), funcionando como os galhos da árvore.
* **`File` (Arquivo):** Estende `FSNode`. Representa a folha da árvore e contém conteúdo (String).

### Fluxo do Journaling
Implementado através da classe `Journal`:
1.  **LogStart:** Antes de qualquer modificação (ex: `mkdir`), exibe timestamp e intenção.
2.  **Commit:** Se a operação for bem-sucedida, confirma a gravação.
3.  **Error:** Se a operação for inválida, aciona o log de erro.

### Classes Principais (`FileSystemSimulator.java`)

| Classe | Responsabilidade |
| :--- | :--- |
| **`FileSystemSimulator`** | Classe principal (`main`). Inicializa o sistema e roda o loop do Shell. |
| **`FileSystem`** | O "cérebro". Mantém referência para o `root` e diretório atual. Contém a lógica de `mkdir`, `touch`, `rm`, `cp`, `rename`. |
| **`Journal`** | Responsável por imprimir as tags `[JOURNAL - WAL]`, simulando o log de transações. |

---

## 🚀 Instalação e Execução

### Pré-requisitos
* Java JDK 8 ou superior instalado.

### Passo a Passo

1.  **Compilação**
    Abra o terminal na pasta do arquivo e execute:
    ```bash
    javac FileSystemSimulator.java
    ```

2.  **Execução**
    Inicie o simulador:
    ```bash
    java FileSystemSimulator
    ```

3.  **Uso (Exemplo de Comandos)**
    O programa abrirá o prompt `usuario@java-fs:/$`. Tente os seguintes comandos:

    ```bash
    mkdir docs          # Cria pasta docs
    cd docs             # Entra na pasta
    touch trabalho.txt  # Cria arquivo
    ls                  # Lista conteúdo
    cd ..               # Volta um nível
    cp docs docs_bkp    # Copia a pasta inteira
    ```

---

## ✅ Resultados Esperados

O simulador demonstra com sucesso a hierarquia de um sistema de arquivos. Ao executar os comandos, é possível visualizar o funcionamento do Journaling através das mensagens de log que precedem as ações, ilustrando como o Sistema Operacional garante a rastreabilidade das operações antes de efetivá-las.

A estrutura de árvore permite uma navegação fluida entre diretórios e a manipulação correta dos nós.

---

## 🔗 Links

O código fonte completo está disponível em:  
https://github.com/jomatheux/Simulador-de-arquivos.git
