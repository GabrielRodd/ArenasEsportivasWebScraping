package com.config;

import com.model.ArenaModel;
import java.sql.*;
import java.util.List;

public class ArenaDAO {

    // O arquivo 'arenas.db' será criado na raiz da pasta do seu projeto
    private String url = "jdbc:sqlite:arenas.db";

    public void salvarArenas(List<ArenaModel> arenas) {
        // SQL para criar a tabela se ela não existir
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS arenas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT UNIQUE," +
                "telefone TEXT," +
                "endereco TEXT," +
                "cidade TEXT," +
                "url_maps TEXT UNIQUE" +
                ");";

        String sqlInsert = "INSERT INTO arenas (nome, telefone, endereco, cidade, url_maps) " +
                "VALUES (?, ?, ?, ?, ?) ON CONFLICT(url_maps) DO NOTHING";

        try (Connection conn = DriverManager.getConnection(url)) {
            // 1. Criar a tabela se for a primeira execução
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sqlCreateTable);
            }

            // 2. Inserir os dados
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                conn.setAutoCommit(false); // Melhora performance em lote

                for (ArenaModel arena : arenas) {
                    pstmt.setString(1, arena.getNome());
                    pstmt.setString(2, arena.getNumero());
                    pstmt.setString(3, arena.getEndereco());
                    pstmt.setString(4, arena.getCidade());
                    pstmt.setString(5, arena.getLinkPaginaGoogleMaps());
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
                System.out.println("Dados salvos com sucesso no SQLite (arenas.db)!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao acessar o SQLite: " + e.getMessage());
        }
    }
}


