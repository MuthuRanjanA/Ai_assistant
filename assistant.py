import pyttsx3
import speech_recognition as sr
import webbrowser as wb
import time
engine = pyttsx3.init('sapi5')
voices = engine.getProperty('voices')
engine.setProperty('voice', voices[0].id)

def speak(audio: str):
    """Converts text to speech."""
    engine.say(audio)
    engine.runAndWait()

def take_command():
    """Takes voice command from user and returns the recognized text."""
    r = sr.Recognizer()
    with sr.Microphone() as source:
        print("Listening...")
        r.adjust_for_ambient_noise(source)
        r.pause_threshold = 1
        audio = r.listen(source)

    try:
        print("Processing...")
        query = r.recognize_google(audio, language="en-in")
        print("User said:", query)
        return query.lower()

    except sr.UnknownValueError:
        speak("I didn't understand. Please say that again.")
        return take_command() 
    except sr.RequestError:
        speak("There is a problem with the speech recognition service.")
        return None

if __name__ == "__main__":
    query = take_command()

    if query and "open browser" in query:
        speak("Opening Google")
        wb.open("https://www.google.com")
        time.sleep(3)

        speak("What do you want to search?")
        search_query = take_command()

        if search_query:
            print("Searching for:", search_query)

            if "image" in search_query or "images of" in search_query:
                search_url = f"https://www.google.com/search?tbm=isch&q={search_query.replace('images of', '').strip()}"
                speak(f"Searching images for {search_query}")
            elif "video" in search_query or "videos of" in search_query:
                search_url = f"https://www.youtube.com/results?search_query={search_query.replace('videos of', '').strip()}"
                speak(f"Searching videos for {search_query}")
            else:
                search_url = f"https://www.google.com/search?q={search_query}"
                speak(f"Searching Google for {search_query}")

            wb.open(search_url) 
