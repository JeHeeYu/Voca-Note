"""Capture the debug UI catalog, never the signed-in activity.

Usage: python3 scripts/capture-screenshots.py --serial <adb-device-id>
Keep the device unlocked. Only task-created screenshot/XML files are written.
"""
import argparse
import pathlib
import re
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--screens', nargs='+', default=['home', 'library', 'settings', 'editor', 'review', 'part-of-speech', 'worksheet', 'worksheet-results'])
args = parser.parse_args()
root = pathlib.Path(__file__).resolve().parents[1]
output = root / 'docs/screenshots'
output.mkdir(parents=True, exist_ok=True)


def adb(*arguments, binary=False):
    for attempt in range(3):
        try:
            return subprocess.check_output(['adb', '-s', args.serial, *arguments], text=not binary, timeout=30)
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
            if attempt == 2:
                raise
            time.sleep(1)


def nodes():
    adb('shell', 'uiautomator', 'dump', '/sdcard/voca-note-catalog.xml')
    return list(ET.fromstring(adb('shell', 'cat', '/sdcard/voca-note-catalog.xml')).iter('node'))


def tap(text):
    matches = [n for n in nodes() if n.get('text') == text or n.get('content-desc') == text]
    if not matches:
        raise RuntimeError(f'Catalog control not visible: {text}')
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', matches[0].get('bounds')))
    adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(0.4)


def launch(screen):
    # Reset the debug-only catalog between screenshots; user account data is untouched.
    adb('shell', 'am', 'force-stop', 'com.example.vocanote')
    adb('shell', 'am', 'start', '-W', '-n', 'com.example.vocanote/.debug.UiCatalogActivity', '--es', 'screen', screen)
    time.sleep(0.8)


def capture(name):
    if not any(n.get('package') == 'com.example.vocanote' for n in nodes()):
        raise RuntimeError('Unlock the device and show the catalog before capturing.')
    # Write the PNG on-device to avoid screencap display warnings in stdout.
    adb('shell', 'screencap', '-p', '/sdcard/voca-note-catalog.png')
    adb('pull', '/sdcard/voca-note-catalog.png', str(output / f'{name}.png'))
    print(f'Captured {name}')


for screen in args.screens:
    launch('editor' if screen == 'part-of-speech' else screen)
    if screen == 'part-of-speech':
        tap('명사 (n)')
        # Reveal the newly added expression types in the scrollable menu.
        for _ in range(4):
            current = nodes()
            if any(n.get('text') == '약어 (abbr)' for n in current):
                break
            scroll = [n for n in current if n.get('scrollable') == 'true'][-1]
            x1, y1, x2, y2 = map(int, re.findall(r'\d+', scroll.get('bounds')))
            adb('shell', 'input', 'swipe', str((x1+x2)//2), str(y2-80), str((x1+x2)//2), str(y1+80), '400')
    capture(screen)

adb('shell', 'rm', '-f', '/sdcard/voca-note-catalog.xml', '/sdcard/voca-note-catalog.png')
