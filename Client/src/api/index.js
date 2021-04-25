import DATA from './data'

let __instance = null

export default class API {
    #token = sessionStorage.getItem('token') || null

    static instance() {
        if (__instance == null)
            __instance = new API()

        return __instance
    }

    async login(email, pass) {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: pass })
        };
        const response = await fetch(`http://localhost:8080/login`, requestOptions);
        if (response.status === 200) {
            localStorage.setItem('user', email)
            localStorage.setItem('token', response.headers.get("Authentication"))
            this.#token = response.headers.get("Authentication")
            return true
        } else if (response.status === 401) {
            return false
        }
    }

    async logout() {
        this.#token = null
        localStorage.clear()

        return true
    }

    async findMovies(
        {
            filter: { genre = '', title = '', status = '' } = { genre: '', title: '', status: '' },
            sort,
            pagination: { page = 0, size = 7 } = { page: 0, size: 7 }
        } = {
                filter: { genre: '', title: '', status: '' },
                sort: {},
                pagination: { page: 0, size: 7 }
            }
    ) {
        return new Promise(resolve => {
            const filtered = DATA.movies
                ?.filter(movie => movie.title.toLowerCase().includes(title.toLowerCase() || ''))
                ?.filter(movie => genre !== '' ? movie.genres.map(genre => genre.toLowerCase()).includes(genre.toLowerCase()) : true)
                ?.filter(movie => movie.status.toLowerCase().includes(status.toLowerCase() || ''))

            const data = {
                content: filtered?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < filtered.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        })
    }

    /*async findMovies() {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };
        const response = await fetch(`http://localhost:8080/films`, requestOptions);
        if (response.status === 200) {
            return await response.json()
        } else if (response.status === 404) {
            this.props.history.push("/404");
        }
        return await response.json()
    }*/

    async findMovie(id) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };
        const response = await fetch(`http://localhost:8080/films/${id}`, requestOptions);
        if (response.status === 200) {
            return await response.json()
        } else if (response.status === 404) {
            this.props.history.push("/404");
        }
        return await response.json()
        //return DATA.movies.find(movie => movie.id === id)
    }

    async findUser(id) {
        const requestOptions = {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": this.#token
            }
        };
        const response = await fetch(`http://localhost:8080/users/${id}`, requestOptions);
        if (response.status === 200) {
            return await response.json()
        } else if (response.status === 404) {
            this.props.history.push("/404");
        }














        //MIRAR POR QUE NO CARGA EL PERFIL BIEN AL HACER F5
        return await response.json()
    }

    async findComments(
        {
            filter: { movie = '', user = '' } = { movie: '', user: '' },
            sort,
            pagination: { page = 0, size = 10 } = { page: 0, size: 10 }
        } = {
                filter: { movie: '', user: '' },
                sort: {},
                pagination: { page: 0, size: 10 }
            }
    ) {
        return new Promise(resolve => {
            const filtered = DATA.comments
                ?.filter(comment => comment?.movie?.id === movie)

            const data = {
                content: filtered?.slice(size * page, size * page + size),
                pagination: {
                    hasNext: size * page + size < filtered.length,
                    hasPrevious: page > 0
                }
            }

            resolve(data)
        })
    }

    async createComment(assessment) {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                rating: assessment.rating,
                user: assessment.user,
                film: assessment.film,
                comment: assessment.comment
            })
        };
        const response = await fetch(`http://localhost:8080/films/assessments`, requestOptions);

        if (response.status === 200) {
            return true
        } else if (response.status === 403) {
            return false
        } else if (response.status === 409) {
            return false
        }
    }

    async createUser(user) {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: user.email,
                name: user.name,
                password: user.password,
                birthday: {
                    day: user.birthday.day,
                    month: user.birthday.month,
                    year: user.birthday.year
                },
                roles: ["ROLE_USER"]
            })
        };
        const response = await fetch(`http://localhost:8080/users`, requestOptions);

        if (response.status === 200) {
            return true
        } else if (response.status === 403) {
            return false
        } else if (response.status === 409) {
            return false
        }
    }

    async updateUser(id, user) {
        const requestOptions = {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                op: "",
                path: "",
                value: ""
            })
        };
        const response = await fetch(`http://localhost:8080/users/${id}`, requestOptions);

        if (response.status === 200) {
            localStorage.setItem('user', user.email)
            localStorage.setItem('token', response.headers.get("Authentication"))
            this.#token = response.headers.get("Authentication")
            return true
        } else if (response.status === 403) {
            return false
        } else if (response.status === 409) {
            return false
        }
    }
}