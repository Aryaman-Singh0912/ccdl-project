import { useEffect, useState } from "react";
import { api } from "./api.js";

const STATUSES = ["TODO", "IN_PROGRESS", "DONE"];
const PRIORITIES = ["LOW", "MEDIUM", "HIGH"];

function AuthForm({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const data = await api("/auth/" + mode, {
        method: "POST",
        body: { email, password },
      });
      onLogin(data.token, data.email);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-card">
      <h1>Smart Task &amp; Note Manager</h1>
      <p className="muted">
        {mode === "login"
          ? "Log in to see your notes and tasks"
          : "Create an account"}
      </p>
      <form onSubmit={submit}>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password (6+ characters)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button disabled={busy}>
          {busy ? "Please wait..." : mode === "login" ? "Log in" : "Sign up"}
        </button>
      </form>
      {error && <p className="error">{error}</p>}
      <p className="muted">
        {mode === "login" ? "New here? " : "Already have an account? "}
        <a
          href="#"
          onClick={(e) => {
            e.preventDefault();
            setError("");
            setMode(mode === "login" ? "register" : "login");
          }}
        >
          {mode === "login" ? "Sign up" : "Log in"}
        </a>
      </p>
    </div>
  );
}

function RichText({ text }) {
  const bold = (s) =>
    s
      .split(/(\*\*[^*]+\*\*)/g)
      .map((part, i) =>
        part.startsWith("**") && part.endsWith("**") && part.length > 4 ? (
          <strong key={i}>{part.slice(2, -2)}</strong>
        ) : (
          part
        ),
      );
  return (
    <div className="rich">
      {text.split("\n").map((line, i) => {
        const t = line.trim();
        if (!t) return <div key={i} className="gap" />;
        if (/^[*-]\s+/.test(t))
          return (
            <div key={i} className="bullet">
              • {bold(t.replace(/^[*-]\s+/, ""))}
            </div>
          );
        return <div key={i}>{bold(t)}</div>;
      })}
    </div>
  );
}

function Notes({ token, onError }) {
  const [notes, setNotes] = useState([]);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [tags, setTags] = useState("");
  const [summarizing, setSummarizing] = useState(null);

  const load = () => api("/notes", { token }).then(setNotes).catch(onError);
  useEffect(() => {
    load();
  }, []);

  async function add(e) {
    e.preventDefault();
    const tagList = tags
      .split(",")
      .map((t) => t.trim())
      .filter(Boolean);
    try {
      await api("/notes", {
        method: "POST",
        token,
        body: { title, content, tags: tagList },
      });
      setTitle("");
      setContent("");
      setTags("");
      load();
    } catch (err) {
      onError(err);
    }
  }

  async function summarize(id) {
    setSummarizing(id);
    try {
      const updated = await api("/notes/" + id + "/summarize", {
        method: "POST",
        token,
      });
      setNotes((list) => list.map((n) => (n.id === id ? updated : n)));
    } catch (err) {
      onError(err);
    }
    setSummarizing(null);
  }

  async function remove(id) {
    try {
      await api("/notes/" + id, { method: "DELETE", token });
      setNotes((list) => list.filter((n) => n.id !== id));
    } catch (err) {
      onError(err);
    }
  }

  return (
    <section className="panel">
      <h2>Notes</h2>
      <form onSubmit={add}>
        <input
          placeholder="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <textarea
          placeholder="Write your note..."
          rows={4}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          required
        />
        <input
          placeholder="Tags (comma separated)"
          value={tags}
          onChange={(e) => setTags(e.target.value)}
        />
        <button>Add note</button>
      </form>
      {notes.length === 0 && <p className="muted">No notes yet.</p>}
      {notes.map((n) => (
        <article key={n.id} className="item">
          <h3>{n.title}</h3>
          <p>{n.content}</p>
          {n.tags && n.tags.length > 0 && (
            <p className="tags">
              {n.tags.map((t) => (
                <span key={t}>#{t}</span>
              ))}
            </p>
          )}
          {n.summary && (
            <div className="summary">
              <strong>AI summary</strong>
              <RichText text={n.summary} />
            </div>
          )}
          <div className="row">
            <button
              className="secondary"
              disabled={summarizing === n.id}
              onClick={() => summarize(n.id)}
            >
              {summarizing === n.id
                ? "Summarizing..."
                : n.summary
                  ? "Summarize again"
                  : "Summarize with AI"}
            </button>
            <button className="danger" onClick={() => remove(n.id)}>
              Delete
            </button>
          </div>
        </article>
      ))}
    </section>
  );
}

function Tasks({ token, onError }) {
  const [tasks, setTasks] = useState([]);
  const [title, setTitle] = useState("");
  const [priority, setPriority] = useState("MEDIUM");
  const [dueDate, setDueDate] = useState("");

  const load = () => api("/tasks", { token }).then(setTasks).catch(onError);
  useEffect(() => {
    load();
  }, []);

  async function add(e) {
    e.preventDefault();
    try {
      await api("/tasks", {
        method: "POST",
        token,
        body: { title, priority, dueDate: dueDate || null },
      });
      setTitle("");
      setDueDate("");
      load();
    } catch (err) {
      onError(err);
    }
  }

  async function setStatus(task, status) {
    try {
      const updated = await api("/tasks/" + task.id, {
        method: "PUT",
        token,
        body: { ...task, status },
      });
      setTasks((list) => list.map((t) => (t.id === task.id ? updated : t)));
    } catch (err) {
      onError(err);
    }
  }

  async function remove(id) {
    try {
      await api("/tasks/" + id, { method: "DELETE", token });
      setTasks((list) => list.filter((t) => t.id !== id));
    } catch (err) {
      onError(err);
    }
  }

  return (
    <section className="panel">
      <h2>Tasks</h2>
      <form onSubmit={add}>
        <input
          placeholder="What needs doing?"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <div className="row">
          <select
            value={priority}
            onChange={(e) => setPriority(e.target.value)}
          >
            {PRIORITIES.map((p) => (
              <option key={p}>{p}</option>
            ))}
          </select>
          <input
            type="date"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
          />
        </div>
        <button>Add task</button>
      </form>
      {tasks.length === 0 && <p className="muted">No tasks yet.</p>}
      {tasks.map((t) => (
        <article
          key={t.id}
          className={"item " + (t.status === "DONE" ? "done" : "")}
        >
          <h3>{t.title}</h3>
          <p className="muted">
            Priority: {t.priority}
            {t.dueDate ? " · Due " + t.dueDate : ""}
          </p>
          <div className="row">
            <select
              value={t.status}
              onChange={(e) => setStatus(t, e.target.value)}
            >
              {STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s.replace("_", " ")}
                </option>
              ))}
            </select>
            <button className="danger" onClick={() => remove(t.id)}>
              Delete
            </button>
          </div>
        </article>
      ))}
    </section>
  );
}

export default function App() {
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [email, setEmail] = useState(localStorage.getItem("email"));
  const [error, setError] = useState("");

  function login(t, e) {
    localStorage.setItem("token", t);
    localStorage.setItem("email", e);
    setToken(t);
    setEmail(e);
    setError("");
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("email");
    setToken(null);
    setEmail(null);
  }

  function handleError(err) {
    if (err.status === 401) {
      logout();
      return;
    }
    setError(err.message);
  }

  if (!token) return <AuthForm onLogin={login} />;

  return (
    <div className="app">
      <header>
        <h1>Smart Task &amp; Note Manager</h1>
        <div className="row">
          <span className="muted">{email}</span>
          <button className="secondary" onClick={logout}>
            Log out
          </button>
        </div>
      </header>
      {error && (
        <p className="error" onClick={() => setError("")}>
          {error} (click to dismiss)
        </p>
      )}
      <main>
        <Notes token={token} onError={handleError} />
        <Tasks token={token} onError={handleError} />
      </main>
    </div>
  );
}
