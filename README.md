# CryptoJavaSwing

Simple Swing app for classic, symmetric, asymmetric, and hash demos.

## Tabs
- Classic: Caesar, Affine, Hill 2x2, Substitution, Vigenere (ENG/VIE)
- Symmetric: AES, ARIA, Camellia, CAST5, CAST6, Serpent, Twofish, Blowfish, DES, 3DES (DESede) when supported by the current JDK provider
- Asymmetric: RSA (Base64 public/private keys)
- Hash: MD2, MD5, SHA-1, SHA-224, SHA-384, SHA-256, SHA-512, SHA-512/224, SHA-512/256, SHAKE128, SHAKE256, BLAKE2B-512, RIPEMD160, Whirlpool

## Features
- Encrypt/decrypt text in Classic, Symmetric, and Asymmetric tabs.
- Hash text and files in the Hash tab.
- Encrypt/decrypt files in Symmetric and Asymmetric tabs.
- Copy keys and save keys to `.txt` files.
- Save text input/output to `.txt` files.

## Notes
- Use Generate key for supported sizes.
- Symmetric supports modes: CBC, ECB, CFB, OFB, CTR.
- Symmetric supports padding: PKCS5Padding, PKCS7Padding, ISO10126Padding, X923Padding, NoPadding, ZeroBytePadding (availability depends on provider).
- Hash file output is shown in the output area (save it with Save output when needed).
- Asymmetric expects Base64 public/private keys.
- RSA text encryption is for short text only.
- RSA file encryption uses a hybrid design: RSA protects a random AES key and IV, while AES encrypts the file content.
- Hill 2x2 adds one padding character when plaintext has an odd number of supported alphabet characters.
