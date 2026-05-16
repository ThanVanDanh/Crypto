# CryptoJavaSwing

Simple Swing app for classic, symmetric, asymmetric, and hash demos.

## Tabs
- Classic: Caesar, Affine, Hill 2x2, Substitution, Vigenere (ENG/VIE)
- Symmetric: AES-CBC, AES-GCM, DES-CBC, 3DES-CBC, Blowfish-CBC, RC2-CBC, ARCFOUR/RC4, ChaCha20, ChaCha20-Poly1305 when supported by the current JDK
- Asymmetric: RSA (Base64 public/private keys)
- Hash: all MessageDigest algorithms reported by the current JDK provider

## Features
- Encrypt/decrypt text in Classic, Symmetric, and Asymmetric tabs.
- Hash text and files in the Hash tab.
- Encrypt/decrypt files in Symmetric and Asymmetric tabs.
- Copy keys and save keys to `.txt` files.
- Save text input/output to `.txt` files.

## Notes
- Use Generate key for supported sizes.
- Symmetric expects Base64 key and Base64 IV/nonce when the algorithm needs one.
- Asymmetric expects Base64 public/private keys.
- RSA text encryption is for short text only.
- RSA file encryption uses a hybrid design: RSA protects a random AES key and IV, while AES encrypts the file content.
- Hill 2x2 adds one padding character when plaintext has an odd number of supported alphabet characters.
