#!/usr/bin/env python3
"""
Genera un hash bcrypt compatible con Spring Security (BCryptPasswordEncoder)
para insertar manualmente en la tabla `credenciales_jurado.password_hash`.

Requiere: pip install bcrypt

Uso:
    python3 hash_jurado.py "MiContraseñaSegura123"

Luego inserta en la base de datos, por ejemplo:

    INSERT INTO credenciales_jurado (id, email, password_hash, fecha_creacion, fecha_actualizacion)
    VALUES (gen_random_uuid(), 'jurado@empresa.com', '<HASH_GENERADO>', now(), now());
"""
import sys
import bcrypt

def main():
    if len(sys.argv) != 2:
        print("Uso: python3 hash_jurado.py <contraseña_en_texto_plano>")
        sys.exit(1)

    password = sys.argv[1].encode("utf-8")
    hashed = bcrypt.hashpw(password, bcrypt.gensalt(rounds=10)).decode("utf-8")
    print(hashed)

if __name__ == "__main__":
    main()
