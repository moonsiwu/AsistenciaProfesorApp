-- =====================================================================
-- Sistema de Gestión de Asistencia - Script de Base de Datos
-- Motor: MySQL 8.x (compatible con MySQL Workbench)
-- =====================================================================

DROP DATABASE IF EXISTS asistencia_db;
CREATE DATABASE asistencia_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE asistencia_db;

-- ---------------------------------------------------------------------
-- Tabla: profesores
-- El profesor es quien inicia sesión y administra la asistencia.
-- ---------------------------------------------------------------------
CREATE TABLE profesores (
    id_profesor     INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100)  NOT NULL,
    usuario         VARCHAR(50)   NOT NULL UNIQUE,
    contrasena      VARCHAR(255)  NOT NULL,   -- se guarda hasheada (SHA-256)
    correo          VARCHAR(100),
    fecha_creacion  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: cursos
-- Cada curso pertenece a un profesor.
-- ---------------------------------------------------------------------
CREATE TABLE cursos (
    id_curso        INT AUTO_INCREMENT PRIMARY KEY,
    nombre_curso    VARCHAR(100)  NOT NULL,
    jornada         VARCHAR(30),
    id_profesor     INT           NOT NULL,
    fecha_creacion  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_curso_profesor
        FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: estudiantes
-- Cada estudiante pertenece a un curso.
-- ---------------------------------------------------------------------
CREATE TABLE estudiantes (
    id_estudiante   INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100)  NOT NULL,
    documento       VARCHAR(30)   NOT NULL UNIQUE,
    id_curso        INT           NOT NULL,
    activo          TINYINT(1)    DEFAULT 1,
    CONSTRAINT fk_estudiante_curso
        FOREIGN KEY (id_curso) REFERENCES cursos(id_curso)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: asistencias
-- Un registro por estudiante, por curso, por fecha.
-- ---------------------------------------------------------------------
CREATE TABLE asistencias (
    id_asistencia   INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante   INT           NOT NULL,
    id_curso        INT           NOT NULL,
    id_profesor     INT           NOT NULL,
    fecha           DATE          NOT NULL,
    estado          ENUM('PRESENTE','AUSENTE','TARDE','EXCUSA') NOT NULL,
    observacion     VARCHAR(255),
    fecha_registro  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asistencia_estudiante
        FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante)
        ON DELETE CASCADE,
    CONSTRAINT fk_asistencia_curso
        FOREIGN KEY (id_curso) REFERENCES cursos(id_curso)
        ON DELETE CASCADE,
    CONSTRAINT fk_asistencia_profesor
        FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor)
        ON DELETE CASCADE,
    -- Evita registrar dos veces al mismo estudiante el mismo día
    CONSTRAINT uq_asistencia_dia UNIQUE (id_estudiante, fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- Datos de prueba
-- =====================================================================

-- Profesor de prueba -> usuario: profesor1  /  contraseña: 123456
-- (el hash corresponde a SHA-256 de "123456")
INSERT INTO profesores (nombre_completo, usuario, contrasena, correo) VALUES
('Carlos Andrés Pérez', 'profesor1',
 '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
 'carlos.perez@institucion.edu.co');

INSERT INTO cursos (nombre_curso, jornada, id_profesor) VALUES
('10-A', 'Mañana', 1),
('11-B', 'Tarde', 1);

INSERT INTO estudiantes (nombre_completo, documento, id_curso) VALUES
('Ana María Gómez', '1001234567', 1),
('Juan Esteban Rojas', '1001234568', 1),
('Laura Sofía Díaz', '1001234569', 1),
('Miguel Ángel Torres', '1001234570', 2),
('Valentina Castro', '1001234571', 2);
