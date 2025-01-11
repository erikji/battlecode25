import sys
import os
import shutil

#usage: python copybot.py SOURCE_BOT_PACKAGE_NAME DEST_BOT_PACKAGE_NAME

def set_package_name(dir: str):
    """
    change the package name at the top of all files in the bot
    might have some edge cases where it doesnt work
    """
    for file in os.listdir(dir):
        if file.rfind('.java') + 5 != len(file):
            continue
        path = os.path.join(dir, file)
        package_name = os.path.basename(dir)
        all_text = []
        with open(path, 'r', encoding='utf-8') as f:
            all_text = f.read().split(';')
        all_text[0] = 'package ' + str(package_name)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(';'.join(all_text))
            f.flush()

def copy(a: str, b: str):
    """
    copy bot from a to b, overwriting if necessary
    also changes the package name at the top of the file
    """
    if os.path.exists(b):
        shutil.rmtree(b)
    shutil.copytree(a, b)
    set_package_name(b)

if __name__ == '__main__':
    print(f'copying {sys.argv[1]} to {sys.argv[2]}')
    copy(os.path.normpath(os.path.join(__file__, '..', 'src', sys.argv[1])), os.path.normpath(os.path.join(__file__, '..', 'src', sys.argv[2])))