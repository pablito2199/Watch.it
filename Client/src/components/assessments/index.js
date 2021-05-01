import { FilmOutline as Film } from '@graywolfai/react-heroicons'
import { Button, Input } from '../'

export function Assessments({ comments, createComment }) {
    let render = []

    render.push(<ObtainComments comments={comments} />)
    render.push(<CreateComment />)

    return render
}

function createRating(rating) {
    let children = []

    for (let i = 0; i < 10; i++) {
        if (i < rating) {
            children.push(<Film className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`} />);
        } else {
            children.push(<Film className={`inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`} />);
        }
    }

    return children
}

function ObtainComments({ comments }) {
    let render = <></>

    if (comments.content != null) {
        render = comments.content.map((comment) =>
            <div key={comment.id} className='h-96 w-4/6 bg-white rounded p-4 flex flex-col shadow-md border-2'>
                <div className='ml-8 mt-4 flex justify-between'>
                    <span className='font-bold'>{comment.user.email}</span>
                    <div className='text-right mr-10'>
                        {
                            createRating(comment.rating)
                        }
                    </div>
                </div>
                <p className='p-8 md:overflow-hidden'>{comment.comment}</p>
            </div>
        );
    }

    return render
}

const submit = async (event) => {
    event.preventDefault()
    const data = new FormData(event.target)

    /*await createComment({
        user: "pablo@gmail.com",
        film: "10191",
        comment: data.get('comment'),
        rating: 9
    })*/

    window.location.reload();
}

function value(rating) {
    let children = []

    for (let i = 1; i <= 10; i++) {
        if (i <= rating) {
            children.push(
                <Film
                    key={i}
                    className={`cursor-pointer inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gradient-to-br from-pink-500 via-red-500 to-yellow-500 text-white`}
                    //onClick={value(this.key)}
                />);
        } else {
            children.push(
                <Film
                    key={i}
                    className={`cursor-pointer inline p-0.5 m-0.5 transform rotate-6 w-4 h-4 rounded-full bg-gray-300 text-white`}
                    //onClick={value(this.key)}
                />);
        }
    }

    return children
}

function CreateComment() {
    return <form className='w-full h-64 mt-10 flex justify-start'
        onSubmit={submit}
    >
        <div className='flex flex-col md:w-64'>
            <p className='font-bold'>Y a ti, ¿qué te ha parecido?</p>
            <div className='mt-4'>
                {
                    value(5)
                }
            </div>
            <Button className='mt-32' type='submit' variant='primary'>Publicar</Button>
        </div>
        <textarea name='search'
            type='text'
            placeholder='Escribe aquí tu comentario y comparte tu opinión con otros usuarios! Pero por favor, evita hacer spoilers...'
            className='p-4 ml-8 bg-white rounded placeholder-gray-400 font-medium border w-full'
        />
    </form>
}